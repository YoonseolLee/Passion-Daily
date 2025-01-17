package com.example.passionDaily.ui.viewmodels

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteFavoriteRepository
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
class FavoritesViewModel @Inject constructor(
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val remoteFavoriteRepository: RemoteFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository,
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), QuoteInteractionHandler {

    companion object {
        private const val KEY_FAVORITE_INDEX = "favorite_quote_index"
        private const val TAG = "FavoritesViewModel"
    }

    private val auth = FirebaseAuth.getInstance()

    // userId를 StateFlow로 변경
    private val _userId = MutableStateFlow(auth.currentUser?.uid ?: "")
    val userId: StateFlow<String> = _userId.asStateFlow()

    // Auth 상태 변경 리스너
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _userId.value = firebaseAuth.currentUser?.uid ?: ""
        if (firebaseAuth.currentUser != null) {
            loadFavorites() // 사용자가 로그인되면 즐겨찾기 로드
        }
    }

    private val _favoriteQuotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    val favoriteQuotes: StateFlow<List<QuoteEntity>> = _favoriteQuotes.asStateFlow()

    val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    val quotes = quoteStateHolder.quotes

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(KEY_FAVORITE_INDEX, 0)
    val currentFavoriteQuote: StateFlow<QuoteEntity?> =
        combine(favoriteQuotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _isFavoriteLoading = MutableStateFlow(false)
    val isFavoriteLoading: StateFlow<Boolean> = _isFavoriteLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var favoritesJob: Job? = null

    init {
        // Auth 상태 변경 리스너 등록
        auth.addAuthStateListener(authStateListener)

        // 초기 로드
        if (auth.currentUser != null) {
            loadFavorites()
        }
    }

    override fun previousQuote() {
        try {
            val quotesSize = _favoriteQuotes.value.size
            savedStateHandle[KEY_FAVORITE_INDEX] = when {
                _currentQuoteIndex.value == 0 && quotesSize > 0 -> quotesSize - 1
                _currentQuoteIndex.value == 0 -> _currentQuoteIndex.value
                else -> _currentQuoteIndex.value - 1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to previous quote", e)
            _error.value = "Unable to navigate"
        }
    }

    override fun nextQuote() {
        try {
            val nextIndex = _currentQuoteIndex.value + 1
            val quotesSize = _favoriteQuotes.value.size

            savedStateHandle[KEY_FAVORITE_INDEX] = when {
                nextIndex >= quotesSize && quotesSize > 0 -> 0
                nextIndex >= quotesSize -> _currentQuoteIndex.value
                else -> nextIndex
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to next quote", e)
            _error.value = "Unable to navigate"
        }
    }

    fun loadFavorites() {
        val currentUserId = userId.value
        if (currentUserId.isEmpty()) {
            Log.d("FavoritesViewModel", "Skipping loadFavorites: User not logged in")
            return
        }

        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            _isFavoriteLoading.value = true

            try {
                localFavoriteRepository.getAllFavorites(currentUserId).collect { favorites ->
                    _favoriteQuotes.value = favorites
                    if (_currentQuoteIndex.value >= favorites.size) {
                        savedStateHandle[KEY_FAVORITE_INDEX] = 0
                    }
                    Log.d("loadFavorites", "Favorites loaded: ${favorites.size} items")
                }
            } catch (e: SQLiteException) {
                Log.e("loadFavorites", "Database error while loading favorites")
            } catch (e: Exception) {
                Log.e("loadFavorites", "Error loading favorites")
            } finally {
                _isFavoriteLoading.value = false
            }
        }
    }

    fun isFavorite(userId: String, quoteId: String, categoryId: Int): Flow<Boolean> {
        return localFavoriteRepository.checkFavoriteEntity(userId, quoteId, categoryId)
            .map { favorite ->
                favorite?.let {
                    it.userId == userId &&
                            it.quoteId == quoteId &&
                            it.categoryId == categoryId
                } ?: false
            }
    }

    fun addFavorite(quoteId: String) {
        val (currentUser, selectedCategory, currentQuote) = getRequiredData(quoteId) ?: return

        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {
                        saveToLocalDatabase(currentUser, selectedCategory, currentQuote)
                    }
                    launch {
                        addFavoriteToFirestore(currentUser, quoteId)
                    }
                }
                loadFavorites()
            } catch (e: Exception) {
                Log.e("addFavorite", "Failed to add favorite", e)
            }
        }
    }

    private fun getRequiredData(quoteId: String): Triple<FirebaseUser, QuoteCategory, Quote>? {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return null
        val selectedCategory = selectedQuoteCategory.value ?: return null
        val currentQuote = quotes.value.find { it.id == quoteId } ?: return null

        return Triple(currentUser, selectedCategory, currentQuote)
    }

    private suspend fun saveToLocalDatabase(
        currentUser: FirebaseUser,
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    ) {
        ensureCategoryExists(selectedCategory)
        ensureQuoteExists(selectedCategory, currentQuote)
        saveFavorite(currentUser, currentQuote.id, selectedCategory)
    }

    private suspend fun ensureCategoryExists(category: QuoteCategory) {
        if (!localQuoteCategoryRepository.isCategoryExists(category.ordinal)) {
            val categoryEntity = QuoteCategoryEntity(
                categoryId = category.ordinal,
                categoryName = category.getLowercaseCategoryId()
            )
            localQuoteCategoryRepository.insertCategory(categoryEntity)
        }
    }

    private suspend fun ensureQuoteExists(
        category: QuoteCategory,
        quote: Quote
    ) {
        if (!localQuoteRepository.isQuoteExistsInCategory(quote.id, category.ordinal)) {
            val quoteEntity = QuoteEntity(
                quoteId = quote.id,
                text = quote.text,
                person = quote.person,
                imageUrl = quote.imageUrl,
                categoryId = category.ordinal
            )
            localQuoteRepository.insertQuote(quoteEntity)
        }
    }

    private suspend fun saveFavorite(
        user: FirebaseUser,
        quoteId: String,
        category: QuoteCategory
    ) {
        val favoriteEntity = FavoriteEntity(
            userId = user.uid,
            quoteId = quoteId,
            categoryId = category.ordinal
        )
        localFavoriteRepository.insertFavorite(favoriteEntity)
    }

    private fun addFavoriteToFirestore(currentUser: FirebaseUser, quoteId: String) {
        viewModelScope.launch {
            try {
                val favoriteData = createFavoriteData(quoteId)
                val newDocumentId = generateNewDocumentId(currentUser)

                remoteFavoriteRepository.addFavoriteToFirestore(
                    currentUser,
                    newDocumentId,
                    favoriteData
                )
            } catch (e: Exception) {
                Log.e("Firestore", "Firestore 즐겨찾기 추가 실패", e)
                throw e
            }
        }
    }

    private fun createFavoriteData(quoteId: String): HashMap<String, String> {
        val category = selectedQuoteCategory.value?.getLowercaseCategoryId() ?: ""

        return hashMapOf(
            "addedAt" to getCurrentFormattedDateTime(),
            "quoteId" to quoteId,
            "category" to category
        )
    }

    private fun getCurrentFormattedDateTime(): String {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    private suspend fun generateNewDocumentId(currentUser: FirebaseUser): String {
        val category = selectedQuoteCategory.value?.getLowercaseCategoryId() ?: ""
        val lastQuoteNumber = remoteFavoriteRepository.getLastQuoteNumber(
            currentUser,
            category
        )
        val newQuoteNumber = String.format("%06d", lastQuoteNumber + 1)
        return "quote_$newQuoteNumber"
    }

    fun removeFavorite(quoteId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val selectedCategory = selectedQuoteCategory.value ?: return

        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {
                        // quote 삭제 시 category_id도 함께 지정
                        localQuoteRepository.deleteQuote(quoteId, selectedCategory.ordinal)
                        localFavoriteRepository.deleteFavorite(
                            currentUser.uid,
                            quoteId,
                            selectedCategory.ordinal
                        )
                    }
                    launch {
                        remoteFavoriteRepository.deleteFavoriteFromFirestore(currentUser, quoteId)
                    }
                }

                loadFavorites()
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 제거 실패", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoritesJob?.cancel()
        // Auth 리스너 제거
        auth.removeAuthStateListener(authStateListener)
    }
}
