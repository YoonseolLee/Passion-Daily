package com.example.passionDaily.favorites.presentation.viewmodel

import android.database.sqlite.SQLiteException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Favorites.KEY_FAVORITE_INDEX
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
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
                    else -> {
                        favoritesStateHolder.updateFavoriteQuotes(emptyList())
                    }
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

    fun isFavorite(quoteId: String, categoryId: Int): Flow<Boolean> {
        return favoritesLoadingManager.checkIfQuoteIsFavorite(quoteId, categoryId)
    }

    override fun addFavorite(quoteId: String) {
        val (selectedCategory, currentQuote) = getRequiredDataForAdd(
            selectedQuoteCategory.value,
            quotes.value,
            quoteId
        ) ?: return

        viewModelScope.launch {
            try {
                favoritesSavingManager.saveToLocalDatabase(
                    selectedCategory,
                    currentQuote
                )
                loadFavorites()
            } catch (e: Exception) {
                when (e) {
                    is IOException, is TimeoutCancellationException -> {
                        handleError(e)
                        loadFavorites()
                    }

                    else -> handleError(e)
                }
            }
        }
    }

    private fun getRequiredDataForAdd(
        selectedCategory: QuoteCategory,
        quotes: List<Quote>,
        quoteId: String
    ): Pair<QuoteCategory, Quote>? {
        return favoritesSavingManager.getRequiredDataForAdd(
            selectedCategory,
            quotes,
            quoteId
        )
    }

    override suspend fun removeFavorite(quoteId: String, categoryId: Int) {

        viewModelScope.launch {
            try {
                favoritesRemoveManager.deleteLocalFavorite(
                    quoteId,
                    categoryId
                )
                loadFavorites()
            } catch (e: Exception) {
                // 실패 시 롤백할 때도 categoryId 함께 사용
                when (e) {
                    is IOException, is TimeoutCancellationException -> {
                        handleError(e)
                        loadFavorites()
                    }

                    else -> handleError(e)
                }
            }
        }
    }

    private fun handleError(e: Exception) {
        when (e) {
            is CancellationException -> throw e
            is IOException -> {
                toastManager.showNetworkErrorToast()
            }

            is FirebaseFirestoreException -> {
                toastManager.showFirebaseErrorToast()
            }

            is SQLiteException -> {
                toastManager.showRoomDatabaseErrorToast()
            }

            is IllegalStateException -> {
                toastManager.showGeneralErrorToast()
            }

            else -> {
                toastManager.showGeneralErrorToast()
            }
        }
    }
}
