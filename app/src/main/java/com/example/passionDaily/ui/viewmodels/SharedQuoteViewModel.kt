package com.example.passionDaily.ui.viewmodels


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.dao.FavoriteDao
import com.example.passionDaily.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.data.local.dao.QuoteDao
import com.example.passionDaily.data.local.dao.UserDao
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val quoteCategoryDao: QuoteCategoryDao
) : ViewModel(), QuoteViewModelInterface {
    private val quoteCategories = QuoteCategory.values().map { it.koreanName }

    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(null)
    override val selectedQuoteCategory: StateFlow<QuoteCategory?> =
        _selectedQuoteCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _currentQuoteIndex = MutableStateFlow(0)
    override val currentQuote: StateFlow<Quote?> =
        combine(_quotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Pagination parameters
    private var lastLoadedQuote: DocumentSnapshot? = null
    private val pageSize = 20

    override fun nextQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val nextIndex = currentIndex + 1
            if (nextIndex >= (_quotes.value.size - 5)) {
                // Load more quotes when approaching the end
                _selectedQuoteCategory.value?.let { loadMoreQuotes(it) }
            }
            if (nextIndex < _quotes.value.size) nextIndex else currentIndex
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            if (currentIndex > 0) currentIndex - 1 else currentIndex
        }
    }

    private fun loadMoreQuotes(category: QuoteCategory) {
        if (_isLoading.value) return  // StateFlow를 체크

        viewModelScope.launch {
            _isLoading.value = true  // StateFlow 업데이트

            try {
                val result = firestore.collection("categories")
                    .document(category.koreanName)
                    .collection("quotes")
                    .orderBy("createdAt")
                    .let { query ->
                        lastLoadedQuote?.let { query.startAfter(it) } ?: query
                    }
                    .limit(pageSize.toLong())
                    .get()
                    .await()  // 코루틴으로 변환

                val newQuotes = result.map { document ->
                    Quote(
                        id = document.id,
                        category = document.getString("category") ?: "",
                        text = document.getString("text") ?: "",
                        person = document.getString("person") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: 0L,
                        shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                    )
                }
                lastLoadedQuote = result.documents.lastOrNull()
                _quotes.update { currentQuotes -> currentQuotes + newQuotes }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching quotes: ${e.message}")
            } finally {
                _isLoading.value = false  // StateFlow 업데이트
            }
        }
    }

    override fun getQuoteCategories(): List<String> {
        return quoteCategories
    }

    override fun shareText(context: Context, text: String) {
        // TODO: 공유 기능 완성
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
        fetchQuotes(category)
    }

    private fun fetchQuotes(selectedCategory: QuoteCategory?) {
        try {
            if (selectedCategory == null) {
                Log.e("FetchQuotes", "Category is null")
                return
            }
            Log.i("FetchQuotes", "selectedCategory : ${selectedCategory}")

            val db = Firebase.firestore
            db.collection("categories")
                .document(selectedCategory.koreanName)
                .collection("quotes")
                .get()
                .addOnSuccessListener { result ->
                    val quotes = result.map { document ->
                        Quote(
                            id = document.id,
                            category = document.getString("category") ?: "",
                            text = document.getString("text") ?: "",
                            person = document.getString("person") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                            modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: 0L,
                            shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                        )
                    }
                    Log.i("FetchQuotes", "quotes : ${quotes}")
                    _quotes.value = quotes
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching quotes: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("FetchQuotes", "Unexpected error: ${e.message}")
        }
    }

    override fun fetchFavoriteQuotes() {
        try {
            val db = Firebase.firestore
            val currentUser = Firebase.auth.currentUser

            if (currentUser == null) {
                Log.e("FetchFavoriteQuotes", "User is not logged in")
                return
            }

            // 사용자의 saved_quotes 컬렉션에서 모든 즐겨찾기 정보를 가져옵니다
            db.collection("favorites")
                .document(currentUser.uid)
                .collection("saved_quotes")
                .get()
                .addOnSuccessListener { savedQuotesSnapshot ->
                    // 각 문서에서 category와 quote_id 정보를 추출합니다
                    val favoriteQuotesInfo = savedQuotesSnapshot.documents.mapNotNull { document ->
                        val category = document.getString("category")
                        val quoteId = document.getString("quote_id")
                        if (category != null && quoteId != null) {
                            Pair(category, quoteId)
                        } else {
                            Log.e("FetchFavoriteQuotes", "Invalid saved quote document: ${document.id}")
                            null
                        }
                    }

                    if (favoriteQuotesInfo.isEmpty()) {
                        _quotes.value = emptyList()
                        return@addOnSuccessListener
                    }

                    // 모든 즐겨찾기 명언을 가져오기 위한 작업들을 생성합니다
                    val quoteTasks = favoriteQuotesInfo.map { (category, quoteId) ->
                        db.collection("categories")
                            .document(category)
                            .collection("quotes")
                            .document(quoteId)
                            .get()
                    }

                    // 모든 작업을 동시에 실행하고 결과를 합칩니다
                    Tasks.whenAllSuccess<DocumentSnapshot>(quoteTasks)
                        .addOnSuccessListener { documentSnapshots ->
                            val quotes = documentSnapshots.mapNotNull { document ->
                                if (document.exists()) {
                                    Quote(
                                        id = document.id,
                                        category = document.getString("category") ?: "",
                                        text = document.getString("text") ?: "",
                                        person = document.getString("person") ?: "",
                                        imageUrl = document.getString("imageUrl") ?: "",
                                        createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                                        modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: 0L,
                                        shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                                    )
                                } else {
                                    Log.e("FetchFavoriteQuotes", "Quote document doesn't exist: ${document.id}")
                                    null
                                }
                            }
                            Log.i("FetchFavoriteQuotes", "Favorite quotes: $quotes")
                            _quotes.value = quotes
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FetchFavoriteQuotes", "Error fetching quotes: ${exception.message}")
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("FetchFavoriteQuotes", "Error fetching saved quotes: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("FetchFavoriteQuotes", "Unexpected error: ${e.message}")
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
                        categoryName = selectedCategory.koreanName
                    )
                    quoteCategoryDao.insertCategory(categoryEntity)
                }

                // 2. Quote가 없으면 추가
                if (!quoteDao.isQuoteExists(quoteId)) {
                    val quoteEntity = QuoteEntity(
                        quoteId = quoteId,
                        text = currentQuote.text,
                        person = currentQuote.person,
                        imageUrl = currentQuote.imageUrl ?: "",
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
            "added_at" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "quote_id" to quoteId,
            "category" to _selectedQuoteCategory.value?.koreanName
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
        return favoriteDao.isQuoteFavorite(quoteId)
    }
}