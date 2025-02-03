package com.example.passionDaily.favorites.presentation.viewmodel

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Favorites.KEY_FAVORITE_INDEX
import com.example.passionDaily.constants.ViewModelConstants.Favorites.TAG
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.favorites.manager.FavoritesLoadingManager
import com.example.passionDaily.favorites.manager.FavoritesRemoveManager
import com.example.passionDaily.favorites.manager.FavoritesSavingManager
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.ui.viewmodels.QuoteInteractionHandler
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuth,
    private val favoritesStateHolder: FavoritesStateHolder,
    private val favoritesLoadingManager: FavoritesLoadingManager,
    private val favoritesSavingManager: FavoritesSavingManager,
    private val favoritesRemoveManager: FavoritesRemoveManager,
    private val toastManager: ToastManager
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
            handleError(e)
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
            handleError(e)
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
            } catch (e: Exception) {
                favoritesLoadingManager.updateIsFavoriteLoading(false)
                handleError(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoritesJob?.cancel()
        firebaseAuth.removeAuthStateListener(authStateListener)
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
                // 동시 실행 보장 (둘 중 하나가 실패하면 즉시 취소됨)
                coroutineScope {
                    launch { saveToLocalDatabase(currentUser, selectedCategory, currentQuote) }
                    launch { addFavoriteToFirestore(currentUser, quoteId, selectedCategory) }
                }
                loadFavorites()
            } catch (e: Exception) {
                handleError(e)
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
    ) {
        viewModelScope.launch {
            try {
                favoritesSavingManager.addFavoriteToFirestore(
                    currentUser,
                    quoteId,
                    selectedCategory
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    suspend fun removeFavorite(quoteId: String, categoryId: Int) {
        val (currentUser, actualCategoryId) = getRequiredDataForRemove(firebaseAuth, categoryId)
            ?: return

        viewModelScope.launch {
            try {
                favoritesRemoveManager.deleteLocalFavorite(
                    currentUser.uid,
                    quoteId,
                    actualCategoryId
                )
                favoritesRemoveManager.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId)
                loadFavorites()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun getRequiredDataForRemove(
        firebaseAuth: FirebaseAuth,
        categoryId: Int
    ): Pair<FirebaseUser, Int>? {
        return favoritesRemoveManager.getRequiredDataForRemove(firebaseAuth, categoryId)
    }

    private fun handleError(e: Exception) {
        when (e) {
            is CancellationException -> throw e
            is IOException -> {
                Log.e(TAG, "Network error details: ${e.message}", e)
                toastManager.showNetworkErrorToast()
            }

            is FirebaseFirestoreException -> {
                Log.e(TAG, "FirebaseFirestore error details: ${e.message}", e)
                toastManager.showFirebaseErrorToast()
            }

            is SQLiteException -> {
                Log.e(TAG, "Room database error details: ${e.message}", e)
                toastManager.showRoomDatabaseErrorToast()
            }

            is IllegalStateException -> {
                Log.e(TAG, "Illegal state error details: ${e.message}", e)
                toastManager.showGeneralErrorToast()
            }

            else -> {
                Log.e(TAG, "Exception details: ${e.message}", e)
                toastManager.showGeneralErrorToast()
            }
        }
    }
}
