package com.example.passionDaily.ui.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.os.NetworkOnMainThreadException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import com.example.passionDaily.R
import com.example.passionDaily.constants.ViewModelConstants
import com.example.passionDaily.constants.ViewModelConstants.Favorites.KEY_FAVORITE_INDEX
import com.example.passionDaily.constants.ViewModelConstants.Favorites.TAG
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.quote.data.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteFavoriteRepository
import com.example.passionDaily.favorites.manager.FavoritesLoadingManager
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val remoteFavoriteRepository: RemoteFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository,
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val stringProvider: StringProvider,
    private val firebaseAuth: FirebaseAuth,
    private val favoritesStateHolder: FavoritesStateHolder,
    private val favoritesLoadingManager: FavoritesLoadingManager
) : ViewModel(), QuoteInteractionHandler {

    private var userId: String = firebaseAuth.currentUser?.uid ?: ""

    val favoriteQuotes = favoritesStateHolder.favoriteQuotes
    val isFavoriteLoading: StateFlow<Boolean> = favoritesStateHolder.isFavoriteLoading
    val error: StateFlow<String?> = favoritesStateHolder.error

    val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    val quotes = quoteStateHolder.quotes

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(
        KEY_FAVORITE_INDEX,
        0
    )

    val currentFavoriteQuote: StateFlow<QuoteEntity?> = createCurrentFavoriteQuoteFlow()

    private var favoritesJob: Job? = null

    init {
        Log.d(TAG, "ViewModel initialization started")
        Log.d(TAG, "StateHolder instance: $favoritesStateHolder")
    }

    private fun createCurrentFavoriteQuoteFlow(): StateFlow<QuoteEntity?> {
        Log.d(TAG, "Creating flow with favoriteQuotes: $favoriteQuotes")
        return combine(favoriteQuotes, _currentQuoteIndex) { quotes, index ->
            Log.d(TAG, "Combining - quotes: $quotes, index: $index")
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

//    private fun createCurrentFavoriteQuoteFlow(): StateFlow<QuoteEntity?> =
//        combine(favoriteQuotes, _currentQuoteIndex) { quotes, index ->
//            quotes.getOrNull(index)
//        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        viewModelScope.launch {
            userId = firebaseAuth.currentUser?.uid ?: ""
            if (firebaseAuth.currentUser != null) {
                loadFavorites()
            }
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        if (firebaseAuth.currentUser != null) {
            loadFavorites()
        }
    }

    override fun previousQuote() {
        try {
            val quotesSize = favoriteQuotes.value.size

            if (quotesSize == 0) return  // 즐겨찾기가 없는 경우

            if (_currentQuoteIndex.value == 0) {
                savedStateHandle[KEY_FAVORITE_INDEX] = quotesSize - 1
            } else {
                savedStateHandle[KEY_FAVORITE_INDEX] = _currentQuoteIndex.value - 1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in previousQuote: ${e.message}")
        }
    }

    override fun nextQuote() {
        try {
            val nextIndex = _currentQuoteIndex.value + 1
            val quotesSize = favoriteQuotes.value.size

            if (quotesSize == 0) return  // 즐겨찾기가 없는 경우

            if (nextIndex >= quotesSize) {
                savedStateHandle[KEY_FAVORITE_INDEX] = 0
            } else {
                savedStateHandle[KEY_FAVORITE_INDEX] = nextIndex
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in nextQuote: ${e.message}")
        }
    }


//    override fun previousQuote() {
//        try {
//            val quotesSize = favoriteQuotes.value.size
//            savedStateHandle[KEY_FAVORITE_INDEX] = when {
//                _currentQuoteIndex.value == 0 && quotesSize > 0 -> quotesSize - 1
//                _currentQuoteIndex.value == 0 -> _currentQuoteIndex.value
//                else -> _currentQuoteIndex.value - 1
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in previousQuote: ${e.message}")
//        }
//    }
//
//    override fun nextQuote() {
//        try {
//            val nextIndex = _currentQuoteIndex.value + 1
//            val quotesSize = favoriteQuotes.value.size
//
//            savedStateHandle[KEY_FAVORITE_INDEX] = when {
//                nextIndex >= quotesSize && quotesSize > 0 -> 0
//                nextIndex >= quotesSize -> _currentQuoteIndex.value
//                else -> nextIndex
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in nextQuote: ${e.message}")
//        }
//    }

    fun loadFavorites() {
        val currentUserId = userId
        if (currentUserId.isEmpty()) {
            Log.d(TAG, "Skipping loadFavorites: User not logged in")
            return
        }

        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            favoritesLoadingManager.updateIsFavoriteLoading(true)

            try {
                withContext(Dispatchers.IO) {
                    favoritesLoadingManager.getAllFavorites(currentUserId)
                        .catch { e ->
                            favoritesLoadingManager.updateIsFavoriteLoading(false)
                            favoritesStateHolder.updateIsFavoriteLoading(false)
                            throw e
                        }
                        .collect { favorites ->
                            handleFavoritesUpdate(favorites)
                            favoritesLoadingManager.updateIsFavoriteLoading(false)
                        }
                }
            } catch (e: Exception) {
                favoritesLoadingManager.updateIsFavoriteLoading(false)
            }
        }
    }

    private fun handleFavoritesUpdate(favorites: List<QuoteEntity>) {
        favoritesLoadingManager.updateFavoriteQuotes(favorites)
        if (_currentQuoteIndex.value >= favorites.size) {
            savedStateHandle[KEY_FAVORITE_INDEX] = 0
        }
    }

    fun isFavorite(userId: String, quoteId: String, categoryId: Int): Flow<Boolean> {
        return favoritesLoadingManager.isFavorite(userId, quoteId, categoryId)
    }

    fun addFavorite(quoteId: String) {
        val (currentUser, selectedCategory, currentQuote) = getRequiredDataForAdd(quoteId) ?: return

        viewModelScope.launch {
            try {
                coroutineScope {
                    launch { saveToLocalDatabase(currentUser, selectedCategory, currentQuote) }
                    launch { addFavoriteToFirestore(currentUser, quoteId) }
                }
                loadFavorites()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding favorite: ${e.message}")
            }
        }
    }

    fun removeFavorite(quoteId: String, categoryId: Int) {
        val (currentUser, actualCategoryId) = getRequiredDataForRemove(quoteId, categoryId)
            ?: return

        viewModelScope.launch {
            try {
                deleteLocalFavorite(currentUser.uid, quoteId, actualCategoryId)

                remoteFavoriteRepository.deleteFavoriteFromFirestore(
                    currentUser,
                    quoteId,
                    categoryId
                )

                loadFavorites()
            } catch (e: Exception) {
                Log.e(TAG, "Error removing favorite: ${e.message}")
            }
        }
    }

    @Transaction
    private suspend fun deleteLocalFavorite(userId: String, quoteId: String, categoryId: Int) {
        try {
            localFavoriteRepository.deleteFavorite(userId, quoteId, categoryId)

            val remainingFavorites =
                localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId)
            if (remainingFavorites.isEmpty()) {
                localQuoteRepository.deleteQuote(quoteId, categoryId)
            }
            Log.d(TAG, "Successfully deleted favorite from local DB")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting favorite: ${e.message}")
            throw e
        }
    }

    private fun getRequiredDataForRemove(
        quoteId: String,
        categoryId: Int
    ): Pair<FirebaseUser, Int>? {
        val currentUser = firebaseAuth.currentUser ?: run {
            Log.d(TAG, "No user logged in")
            return null
        }

        return Pair(currentUser, categoryId)
    }

    private fun getRequiredDataForAdd(quoteId: String): Triple<FirebaseUser, QuoteCategory, Quote>? {
        val currentUser = firebaseAuth.currentUser ?: run {
            Log.d(TAG, "No user logged in")
            return null
        }
        val selectedCategory = selectedQuoteCategory.value ?: run {
            Log.d(TAG, "No category selected")
            return null
        }
        val currentQuote = quotes.value.find { it.id == quoteId } ?: run {
            Log.d(TAG, "Quote not found: $quoteId")
            return null
        }

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
        val category = selectedQuoteCategory.value?.getLowercaseCategoryId().orEmpty()

        return hashMapOf(
            stringProvider.getString(R.string.added_at) to getCurrentFormattedDateTime(),
            stringProvider.getString(R.string.quote_id) to quoteId,
            stringProvider.getString(R.string.category) to category
        )
    }


    private fun getCurrentFormattedDateTime(): String =
        LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern(stringProvider.getString(R.string.datetime_format)))

    private suspend fun generateNewDocumentId(currentUser: FirebaseUser): String {
        val category = selectedQuoteCategory.value?.getLowercaseCategoryId().orEmpty()
        val lastQuoteNumber = remoteFavoriteRepository.getLastQuoteNumber(currentUser, category)
        val newQuoteNumber = String.format(
            stringProvider.getString(R.string.quote_number_format),
            lastQuoteNumber + 1
        )
        return stringProvider.getString(R.string.quote_id_prefix) + newQuoteNumber
    }

    override fun onCleared() {
        super.onCleared()
        favoritesJob?.cancel()
        firebaseAuth.removeAuthStateListener {}
    }
}
