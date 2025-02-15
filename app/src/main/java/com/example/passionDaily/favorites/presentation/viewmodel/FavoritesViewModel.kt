package com.example.passionDaily.favorites.presentation.viewmodel

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Favorites.KEY_FAVORITE_INDEX
import com.example.passionDaily.constants.ViewModelConstants.Favorites.TAG
import com.example.passionDaily.favorites.base.FavoritesViewModelActions
import com.example.passionDaily.favorites.base.FavoritesViewModelState
import com.example.passionDaily.favorites.manager.FavoritesLoadingManager
import com.example.passionDaily.favorites.manager.FavoritesRemoveManager
import com.example.passionDaily.favorites.manager.FavoritesSavingManager
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import com.example.passionDaily.login.state.AuthState
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quote.presentation.components.QuoteInteractionHandler
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
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
    private val toastManager: ToastManager,
    private val authStateHolder: AuthStateHolder
) : ViewModel(), QuoteInteractionHandler, FavoritesViewModelActions, FavoritesViewModelState {

    override val favoriteQuotes = favoritesStateHolder.favoriteQuotes
    override val isFavoriteLoading: StateFlow<Boolean> = favoritesStateHolder.isFavoriteLoading
    override val error: StateFlow<String?> = favoritesStateHolder.error

    override val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    override val quotes = quoteStateHolder.quotes

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(
        KEY_FAVORITE_INDEX,
        0
    )

    override val currentFavoriteQuote: StateFlow<QuoteEntity?> = createCurrentFavoriteQuoteFlow()

    private var favoritesJob: Job? = null

    private fun createCurrentFavoriteQuoteFlow(): StateFlow<QuoteEntity?> {
        return combine(favoriteQuotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    init {
        viewModelScope.launch {
            authStateHolder.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> loadFavorites()
                    else -> favoritesStateHolder.updateFavoriteQuotes(emptyList())
                }
            }
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

    override fun loadFavorites() {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            Log.d("loadFavorites", "authState: ${authStateHolder.authState.value}")
            when (val state = authStateHolder.authState.value) {
                is AuthState.Authenticated -> {
                    favoritesLoadingManager.updateIsFavoriteLoading(true)
                    try {
                        favoritesLoadingManager.getAllFavorites(state.userId)
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
                else -> {
                    // 로그아웃 상태에서는 빈 리스트로 업데이트
                    favoritesStateHolder.updateFavoriteQuotes(emptyList())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoritesJob?.cancel()
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

    override fun addFavorite(quoteId: String) {
        val (currentUser, selectedCategory, currentQuote) = getRequiredDataForAdd(
            firebaseAuth.currentUser,
            selectedQuoteCategory.value,
            quotes.value,
            quoteId
        ) ?: return

        viewModelScope.launch {
            try {
                // Firestore에 먼저 저장 시도
                favoritesSavingManager.addFavoriteToFirestore(
                    currentUser,
                    quoteId,
                    selectedCategory
                ).also {
                    // Firestore 저장 성공 시 로컬에 저장
                    favoritesSavingManager.saveToLocalDatabase(
                        currentUser,
                        selectedCategory,
                        currentQuote
                    )
                    loadFavorites()
                }
            } catch (e: Exception) {
                // 네트워크 에러나 타임아웃 발생 시
                when (e) {
                    is IOException, is TimeoutCancellationException -> {
                        handleError(e)
                        Log.d(TAG, "e: ${e.message}")
                        loadFavorites()
                    }
                    else -> handleError(e)
                }
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

    override suspend fun removeFavorite(quoteId: String, categoryId: Int) {
        val (currentUser, actualCategoryId) = getRequiredDataForRemove(firebaseAuth, categoryId)
            ?: return

        viewModelScope.launch {
            try {
                favoritesRemoveManager.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId)
                    .also {
                        favoritesRemoveManager.deleteLocalFavorite(
                            currentUser.uid,
                            quoteId,
                            actualCategoryId
                        )
                        loadFavorites()
                    }
            } catch (e: Exception) {
                // 네트워크 에러나 타임아웃 발생 시
                when (e) {
                    is IOException, is TimeoutCancellationException -> {
                        handleError(e)
                        // 즐겨찾기 상태를 원래대로 되돌리기 위해 목록 다시 로드
                        loadFavorites()
                    }
                    else -> handleError(e)
                }
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
