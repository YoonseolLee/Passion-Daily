package com.example.passionDaily.ui.viewmodels


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.dao.FavoriteDao
import com.example.passionDaily.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.data.local.dao.QuoteDao
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalFavoriteRepositoryImpl
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.domain.usecase.QuoteUseCases
import com.example.passionDaily.util.FavoriteQuoteId
import com.example.passionDaily.util.QuoteCategory
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SharedQuoteViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val favoriteDao: FavoriteDao,
    private val quoteDao: QuoteDao,
    private val quoteCategoryDao: QuoteCategoryDao,
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val quoteUseCases: QuoteUseCases,
) : ViewModel(), QuoteViewModelInterface {
    // Pagination parameters
    companion object {
        private var lastLoadedQuote: DocumentSnapshot? = null
        private val pageSize: Int = 20
        private val loadingThreshold: Int = 10
    }

    private val quoteCategories = QuoteCategory.values().map { it.koreanName }

    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(QuoteCategory.EFFORT)
    override val selectedQuoteCategory: StateFlow<QuoteCategory?> =
        _selectedQuoteCategory.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<FavoriteQuoteId>>(emptySet())
    override val favoriteIds: StateFlow<Set<FavoriteQuoteId>> = _favoriteIds.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    override val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _currentQuoteIndex = MutableStateFlow(0)
    override val currentQuote: StateFlow<Quote?> =
        combine(_quotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasReachedEnd = MutableStateFlow(false)
    private val _hasReachedStart = MutableStateFlow(true)

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            // 즐겨찾기 데이터 초기화
            launch {
                localFavoriteRepository.getAllFavoriteIdsWithCategory(userId)
                    .collect { favorites ->
                        _favoriteIds.value = favorites.map { (quoteId, categoryId) ->
                            FavoriteQuoteId(quoteId, categoryId)
                        }.toSet()
                    }
            }

            // 초기 데이터 로드
            launch {
                _selectedQuoteCategory.value?.let { category ->
                    loadQuotes(category)
                }
            }
        }
    }

    override fun nextQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val nextIndex = currentIndex + 1

            // 마지막 인덱스에 도달했고 더 로드할 데이터가 있다면
            if (nextIndex >= _quotes.value.size && !_hasReachedEnd.value) {
                _selectedQuoteCategory.value?.let { category ->
                    if (!_isLoading.value && lastLoadedQuote != null) {
                        loadQuotes(category)
                    }
                }
                currentIndex // 로딩 중에는 현재 인덱스 유지
            } else if (nextIndex >= _quotes.value.size) {
                0 // 마지막 데이터까지 모두 로드된 경우에만 처음으로 순환
            } else {
                nextIndex
            }
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            if (currentIndex == 0) {
                // 첫 페이지이고 이전 데이터가 없다면 마지막으로 이동 시도
                if (_hasReachedEnd.value) {
                    _quotes.value.size - 1
                } else {
                    currentIndex // 이전 데이터가 있다면 현재 위치 유지
                }
            } else {
                currentIndex - 1
            }
        }
    }

    fun loadQuotes(category: QuoteCategory) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            Log.d("SharedQuoteViewModel", "Using pageSize: $pageSize") // 로그 추가

            try {
                val result = remoteQuoteRepository.getQuotesByCategory(
                    category = category,
                    pageSize = pageSize,
                    lastLoadedQuote = lastLoadedQuote
                )

                Log.d("loadQuotes", "result: $result")

                if (result.quotes.isNotEmpty()) {
                    lastLoadedQuote = result.lastDocument
                    _quotes.update { currentQuotes ->
                        if (lastLoadedQuote == null) result.quotes
                        else currentQuotes + result.quotes
                    }
                } else {
                    _hasReachedEnd.value = true
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching quotes: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun getQuoteCategories(): List<String> {
        return quoteCategories
    }

    override fun shareText(context: Context, text: String) {
        quoteUseCases.shareText(context, text)
    }

    override fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        category?.let {
            viewModelScope.launch {
                try {
                    remoteQuoteRepository.incrementShareCount(quoteId, it)
                } catch (e: Exception) {
                    Log.e("ShareCount", "Error incrementing share count", e)
                }
            }
        } ?: run {
            Log.e("ShareCount", "Category is null")
        }
    }

    fun onCategorySelected(category: QuoteCategory?) {
        _selectedQuoteCategory.value = category
        lastLoadedQuote = null  // 페이지네이션 상태 초기화
        _currentQuoteIndex.value = 0  // 현재 인덱스도 초기화
        _quotes.value = emptyList()  // 기존 quotes 초기화
        category?.let { loadQuotes(it) }
    }

    override fun fetchFavoriteQuotes() {
        viewModelScope.launch {
            val favoriteQuotes = remoteQuoteRepository.getFavoriteQuotes()
            _quotes.value = favoriteQuotes
        }
    }

    override fun addFavorite(quoteId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val selectedCategory = _selectedQuoteCategory.value ?: return
        val currentQuote = _quotes.value.find { it.id == quoteId } ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Category가 없으면 추가
                if (!quoteCategoryDao.isCategoryExists(selectedCategory.ordinal)) {
                    val categoryEntity = QuoteCategoryEntity(
                        categoryId = selectedCategory.ordinal,
                        categoryName = selectedCategory.getLowercaseCategoryId()
                    )
                    quoteCategoryDao.insertCategory(categoryEntity)
                }

                // 2. Quote가 없으면 추가
                if (!quoteDao.isQuoteExists(quoteId)) {
                    val quoteEntity = QuoteEntity(
                        quoteId = quoteId,
                        text = currentQuote.text,
                        person = currentQuote.person,
                        imageUrl = currentQuote.imageUrl,
                        categoryId = selectedCategory.ordinal
                    )
                    quoteDao.insertQuote(quoteEntity)
                }

                // 3. Favorite 추가
                coroutineScope {
                    launch {
                        val favoriteEntity = FavoriteEntity(
                            userId = currentUser.uid,
                            quoteId = quoteId
                        )
                        favoriteDao.insertFavorite(favoriteEntity)
                    }
                    launch { addFavoriteToFirestore(currentUser, quoteId) }
                }
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 추가 실패", e)
                // TODO: 에러 처리
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun addFavoriteToFirestore(currentUser: FirebaseUser, quoteId: String) {
        val favoriteData = hashMapOf(
            "addedAt" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "quoteId" to quoteId,
            "category" to _selectedQuoteCategory.value?.getLowercaseCategoryId()
        )

        try {
            firestore.collection("favorites")
                .document(currentUser.uid)
                .set(hashMapOf<String, Any>(), SetOptions.merge())
                .await()

            firestore.collection("favorites")
                .document(currentUser.uid)
                .collection("saved_quotes")
                .document(quoteId)
                .set(favoriteData)
                .await()
        } catch (e: Exception) {
            Log.e("Firestore", "Firestore 즐겨찾기 추가 실패", e)
            throw e
        }
    }

    override fun removeFavorite(quoteId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                coroutineScope {
                    launch {
                        val favoriteEntity = FavoriteEntity(
                            userId = currentUser.uid,
                            quoteId = quoteId,
                            addedAt = System.currentTimeMillis()
                        )
                        favoriteDao.deleteFavorite(favoriteEntity)
                    }
                    launch {
                        firestore.collection("favorites")
                            .document(currentUser.uid)
                            .collection("saved_quotes")
                            .document(quoteId)
                            .delete()
                            .await()
                    }
                }
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 제거 실패", e)
                // TODO: 에러 처리
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun isFavorite(quoteId: String): Flow<Boolean> {
        return favoriteIds.combine(_selectedQuoteCategory) { favorites, category ->
            favorites.any { it.quoteId == quoteId && it.categoryId == category?.ordinal }
        }
    }
}