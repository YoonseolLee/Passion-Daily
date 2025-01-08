package com.example.passionDaily.ui.viewmodels


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.dao.FavoriteDao
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteFavoriteRepository
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.domain.usecase.QuoteUseCases
import com.example.passionDaily.util.FavoriteQuoteId
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SharedQuoteViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val favoriteDao: FavoriteDao,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository,
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val remoteFavoriteRepository: RemoteFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val quoteUseCases: QuoteUseCases,
) : ViewModel(), QuoteViewModelInterface {

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

    private val _favoriteQuotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    val favoriteQuotes: StateFlow<List<QuoteEntity>> = _favoriteQuotes.asStateFlow()

    private val _currentQuoteIndex = MutableStateFlow(0)
    override val currentQuote: StateFlow<Quote?> =
        combine(_quotes, _favoriteQuotes, _currentQuoteIndex) { quotes, favorites, index ->
            when {
                // 일반 명언 목록을 보고 있을 때
                quotes.isNotEmpty() -> quotes.getOrNull(index)
                // 즐겨찾기 목록을 보고 있을 때
                favorites.isNotEmpty() -> favorites.getOrNull(index)?.let { entity ->
                    Quote(
                        id = entity.quoteId,
                        category = QuoteCategory.values().first { it.ordinal == entity.categoryId },
                        text = entity.text,
                        person = entity.person,
                        imageUrl = entity.imageUrl,
                        createdAt = "",
                        modifiedAt = "",
                        shareCount = 0
                    )
                }
                else -> null
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
//    override val currentQuote: StateFlow<Quote?> =
//        combine(_quotes, _currentQuoteIndex) { quotes, index ->
//            quotes.getOrNull(index)
//        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

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
            val currentList = if (_quotes.value.isNotEmpty()) _quotes.value else _favoriteQuotes.value
            val nextIndex = currentIndex + 1

            when {
                // Firestore quotes를 보고 있을 때
                _quotes.value.isNotEmpty() -> {
                    if (nextIndex >= currentList.size && !_hasReachedEnd.value) {
                        _selectedQuoteCategory.value?.let { category ->
                            if (!_isLoading.value && lastLoadedQuote != null) {
                                loadQuotes(category)
                            }
                        }
                        currentIndex
                    } else if (nextIndex >= currentList.size) {
                        0
                    } else {
                        nextIndex
                    }
                }
                // 즐겨찾기를 보고 있을 때
                else -> {
                    if (nextIndex >= currentList.size) 0 else nextIndex
                }
            }
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val currentList = if (_quotes.value.isNotEmpty()) _quotes.value else _favoriteQuotes.value

            if (currentIndex == 0) {
                if (_quotes.value.isNotEmpty() && _hasReachedEnd.value) {
                    currentList.size - 1
                } else {
                    currentIndex
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
            localFavoriteRepository.getAllFavoriteIdsWithCategory(userId)
                .collect { favoriteQuotes ->
                    val quoteEntities = favoriteQuotes.mapNotNull { favorite ->
                        localQuoteRepository.getQuoteById(favorite.quoteId)
                    }
                    _favoriteQuotes.value = quoteEntities
                    _currentQuoteIndex.value = 0
                }
        }
    }

    override fun addFavorite(quoteId: String) {
        // TODO: 코루틴 도전
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val selectedCategory = _selectedQuoteCategory.value ?: return
        val currentQuote = _quotes.value.find { it.id == quoteId } ?: return

        viewModelScope.launch {
            try {
                if (!localQuoteCategoryRepository.isCategoryExists(selectedCategory.ordinal)) {
                    // 1. 카테고리가 없으면 기기에 추가
                    val categoryEntity = QuoteCategoryEntity(
                        categoryId = selectedCategory.ordinal,
                        categoryName = selectedCategory.getLowercaseCategoryId()
                    )
                    localQuoteCategoryRepository.insertCategory(categoryEntity)
                }
                Log.d("Favorite", "insertCategory: $quoteId")

                // 2. Quote가 없으면 기기에 추가
                if (!localQuoteRepository.isQuoteExists(quoteId)) {
                    val quoteEntity = QuoteEntity(
                        quoteId = quoteId,
                        text = currentQuote.text,
                        person = currentQuote.person,
                        imageUrl = currentQuote.imageUrl,
                        categoryId = selectedCategory.ordinal
                    )
                    localQuoteRepository.insertQuote(quoteEntity)
                }
                Log.d("Favorite", "즐겨찾기 추가: $quoteId")
                // 3. Favorite에 추가
                coroutineScope {
                    launch {
                        val favoriteEntity = FavoriteEntity(
                            userId = currentUser.uid,
                            quoteId = quoteId,
                            categoryId = selectedCategory.ordinal,
                        )
                        localFavoriteRepository.insertFavorite(favoriteEntity)
                    }
                    launch { addFavoriteToFirestore(currentUser, quoteId) }
                }
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 추가 실패", e)
            }
        }
    }

    override fun isFavorite(userId: String, quoteId: String, categoryId: Int): Flow<Boolean> {
        return localFavoriteRepository.checkFavoriteEntity(userId, quoteId, categoryId)
            .map { favorite ->
                favorite?.let {
                    it.userId == userId &&
                            it.quoteId == quoteId &&
                            it.categoryId == categoryId
                } ?: false
            }
    }

    private fun addFavoriteToFirestore(currentUser: FirebaseUser, quoteId: String) {

        val favoriteData = hashMapOf(
            "addedAt" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "quoteId" to quoteId,
            "category" to _selectedQuoteCategory.value?.getLowercaseCategoryId()
        )

        viewModelScope.launch {
            try {
                remoteFavoriteRepository.addFavoriteToFirestore(currentUser, quoteId, favoriteData)
            } catch (e: Exception) {
                Log.e("Firestore", "Firestore 즐겨찾기 추가 실패", e)
                throw e
            }
        }
    }

    override fun removeFavorite(quoteId: String) {

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {

                        localQuoteRepository.deleteQuote(quoteId)
                        localFavoriteRepository.deleteFavorite(userId, quoteId, _selectedQuoteCategory.value!!.ordinal)
                    }
                    launch {
                        remoteFavoriteRepository.deleteFavoriteFromFirestore(currentUser, quoteId)
                    }
                }
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 제거 실패", e)
            }
        }
    }
}