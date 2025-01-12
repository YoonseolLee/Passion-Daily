package com.example.passionDaily.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteFavoriteRepository
import com.example.passionDaily.ui.state.QuoteStateHolder
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
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

    private var favoritesJob: Job? = null

    init {
        if (userId.isEmpty()) {
            Log.e("FavoritesViewModel", "User ID is empty")
        }
    }


    override fun previousQuote() {
        val quotesSize = _favoriteQuotes.value.size
        savedStateHandle[KEY_FAVORITE_INDEX] = when {
            _currentQuoteIndex.value == 0 && quotesSize > 0 -> quotesSize - 1
            _currentQuoteIndex.value == 0 -> _currentQuoteIndex.value
            else -> _currentQuoteIndex.value - 1
        }
    }

    override fun nextQuote() {
        val nextIndex = _currentQuoteIndex.value + 1
        val quotesSize = _favoriteQuotes.value.size

        savedStateHandle[KEY_FAVORITE_INDEX] = when {
            nextIndex >= quotesSize && quotesSize > 0 -> 0
            nextIndex >= quotesSize -> _currentQuoteIndex.value
            else -> nextIndex
        }
    }

    fun loadFavorites() {
        if (userId.isEmpty()) {
            Log.e("FavoritesViewModel", "User ID is empty")
            return
        }

        // 기존 Job 취소
        favoritesJob?.cancel()

        favoritesJob = viewModelScope.launch {
            _isFavoriteLoading.value = true
            try {
                // Flow 수집 시작
                localFavoriteRepository.getAllFavorites(userId).collect { favorites ->
                    _favoriteQuotes.value = favorites

                    // 인덱스가 범위를 벗어났다면 0으로 리셋
                    if (_currentQuoteIndex.value >= favorites.size) {
                        savedStateHandle[KEY_FAVORITE_INDEX] = 0
                    }

                    Log.d("loadFavorites", "Favorites loaded: ${favorites.size} items")
                }
            } catch (e: Exception) {
                Log.e("loadFavorites", "Error fetching favorites: ${e.message}")
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
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val selectedCategory = selectedQuoteCategory.value ?: return
        val currentQuote = quotes.value.find { it.id == quoteId } ?: return

        viewModelScope.launch {
            try {
                // 즐겨찾기 추가 후 현재 인덱스 업데이트
                coroutineScope {
                    launch {
                        if (!localQuoteCategoryRepository.isCategoryExists(selectedCategory.ordinal)) {
                            val categoryEntity = QuoteCategoryEntity(
                                categoryId = selectedCategory.ordinal,
                                categoryName = selectedCategory.getLowercaseCategoryId()
                            )
                            localQuoteCategoryRepository.insertCategory(categoryEntity)
                        }

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

                        val favoriteEntity = FavoriteEntity(
                            userId = currentUser.uid,
                            quoteId = quoteId,
                            categoryId = selectedCategory.ordinal,
                        )
                        localFavoriteRepository.insertFavorite(favoriteEntity)
                    }
                    launch { addFavoriteToFirestore(currentUser, quoteId) }
                }

                // 즐겨찾기 추가 후 목록 새로고침
                loadFavorites()
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 추가 실패", e)
            }
        }
    }

    private fun addFavoriteToFirestore(currentUser: FirebaseUser, quoteId: String) {
        val category = selectedQuoteCategory.value?.getLowercaseCategoryId() ?: ""

        val favoriteData = hashMapOf(
            "addedAt" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "quoteId" to quoteId,
            "category" to category  // non-nullable String 사용
        )

        viewModelScope.launch {
            try {
                val lastQuoteNumber = remoteFavoriteRepository.getLastQuoteNumber(
                    currentUser,
                    category
                )

                val newQuoteNumber = String.format("%06d", lastQuoteNumber + 1)
                val newDocumentId = "quote_$newQuoteNumber"

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

    fun removeFavorite(quoteId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {
                        localQuoteRepository.deleteQuote(quoteId)
                        localFavoriteRepository.deleteFavorite(
                            userId,
                            quoteId,
                            selectedQuoteCategory.value!!.ordinal
                        )
                    }
                    launch {
                        remoteFavoriteRepository.deleteFavoriteFromFirestore(currentUser, quoteId)
                    }
                }

                // 즐겨찾기 제거 후 목록 새로고침
                loadFavorites()
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 제거 실패", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoritesJob?.cancel()
    }
}
