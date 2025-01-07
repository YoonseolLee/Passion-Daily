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
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
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
    private val repository: RemoteQuoteRepositoryImpl
) : ViewModel(), QuoteViewModelInterface {
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

    init {
        viewModelScope.launch {
            // 즐겨찾기 데이터 초기화
            launch {
                favoriteDao.getAllFavoriteIdsWithCategory()
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

    // Pagination parameters
    private var lastLoadedQuote: DocumentSnapshot? = null
    private val pageSize = 20
    private val loadingThreshold = 10

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

    private fun loadQuotes(category: QuoteCategory) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val query = firestore.collection("categories")
                    .document(category.toString())
                    .collection("quotes")
                    .orderBy("createdAt")
                    .let { query ->
                        lastLoadedQuote?.let { query.startAfter(it) } ?: query
                    }
                    .limit(pageSize.toLong())

                val result = query.get().await()

                if (!result.isEmpty) {
                    val newQuotes = result.map { document ->
                        Quote(
                            id = document.id,
                            category = QuoteCategory.fromEnglishName(document.getString("category") ?: "")
                                ?: QuoteCategory.OTHER,
                            text = document.getString("text") ?: "",
                            person = document.getString("person") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            createdAt = document.getString("createdAt") ?: "1970-01-01 00:00",
                            modifiedAt = document.getString("modifiedAt") ?: "1970-01-01 00:00",
                            shareCount = document.getLong("shareCount")?.toInt() ?: 0
                        )
                    }

                    lastLoadedQuote = result.documents.lastOrNull()
                    _quotes.update { currentQuotes ->
                        if (lastLoadedQuote == null) newQuotes else currentQuotes + newQuotes
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
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 공유 타입 설정
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "공유하기")
        context.startActivity(chooser)
    }

    override fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        category?.let {
            firestore.collection("categories")
                .document(it.koreanName)
                .collection("quotes")
                .document(quoteId)
                .update("shareCount", FieldValue.increment(1))
                .addOnSuccessListener {
                    Log.d("ShareCount", "Share count incremented successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ShareCount", "Error incrementing share count", e)
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
        category?.let { loadQuotes(it)}
    }

    override fun fetchFavoriteQuotes() {
        // TODO: _quotes를 _favoritequotes로 변경 후 페이지네이션 적용
        viewModelScope.launch {
            val favoriteQuotes  = repository.getFavoriteQuotes()
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
                        categoryName = selectedCategory.toString()
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
            "category" to _selectedQuoteCategory.value?.toString()
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