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
import com.example.passionDaily.favorites.manager.FavoritesSavingManager
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
    private val favoritesLoadingManager: FavoritesLoadingManager,
    private val favoritesSavingManager: FavoritesSavingManager
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
        return combine(favoriteQuotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

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
        return favoritesLoadingManager.checkIfQuoteIsFavorite(userId, quoteId, categoryId)
    }

    fun addFavorite(quoteId: String) {
        val (currentUser, selectedCategory, currentQuote) = getRequiredDataForAdd(
            firebaseAuth.currentUser,
            selectedQuoteCategory.value,
            quotes.value,
            quoteId
        ) ?: return

        viewModelScope.launch {
            try {
                launch { saveToLocalDatabase(currentUser, selectedCategory, currentQuote) }
                launch { addFavoriteToFirestore(currentUser, quoteId, selectedCategory) }
                loadFavorites()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding favorite: ${e.message}")
            }
        }
    }

    private fun getRequiredDataForAdd(
        currentUser: FirebaseUser?,
        selectedCategory: QuoteCategory,
        quotes: List<Quote>,
        quoteId: String
    ): Triple<FirebaseUser, QuoteCategory, Quote>? {
        return favoritesSavingManager.getRequiredDataForAdd(
            currentUser,
            selectedCategory,
            quotes,
            quoteId
        )
    }

    private suspend fun saveToLocalDatabase(
        currentUser: FirebaseUser,
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    ) {
        favoritesSavingManager.saveToLocalDatabase(currentUser, selectedCategory, currentQuote)
    }

    private fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        selectedCategory: QuoteCategory
    ){
        viewModelScope.launch {
            try {
                favoritesSavingManager.addFavoriteToFirestore(currentUser, quoteId, selectedCategory)
            } catch (e: Exception) {
                Log.e("Firestore", "Firestore 즐겨찾기 추가 실패", e)
                throw e
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

    override fun onCleared() {
        super.onCleared()
        favoritesJob?.cancel()
        firebaseAuth.removeAuthStateListener {}
    }
}
