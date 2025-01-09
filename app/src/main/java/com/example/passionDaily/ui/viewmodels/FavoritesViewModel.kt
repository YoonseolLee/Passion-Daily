package com.example.passionDaily.ui.viewmodels

import android.util.Log
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
    private val quoteStateHolder: QuoteStateHolder
) : ViewModel(), QuoteViewModelInterface {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _favoriteQuotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    val favoriteQuotes: StateFlow<List<QuoteEntity>> = _favoriteQuotes.asStateFlow()

    val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    val isLoading = quoteStateHolder.isLoading
    val quotes = quoteStateHolder.quotes

    private val _currentQuoteIndex = MutableStateFlow(0)
    val currentQuote: StateFlow<QuoteEntity?> = combine(
        favoriteQuotes,
        _currentQuoteIndex
    ) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            fetchFavoriteQuotes()
        }
    }

    fun fetchFavoriteQuotes() {
        viewModelScope.launch {
            localFavoriteRepository.getAllFavorites(userId)
                .collect { quotes ->
                    _favoriteQuotes.value = quotes
                }
        }
    }

    override fun nextQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val quotesSize = _favoriteQuotes.value.size
            when {
                quotesSize == 0 -> 0
                currentIndex >= quotesSize - 1 -> 0
                else -> currentIndex + 1
            }
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val quotesSize = _favoriteQuotes.value.size
            when {
                quotesSize == 0 -> 0
                currentIndex == 0 -> quotesSize - 1
                else -> currentIndex - 1
            }
        }
    }

    fun loadFavorites(userId: String) {
        if (quoteStateHolder.isLoading.value) return

        viewModelScope.launch {
            quoteStateHolder.startLoading()

            try {
                localFavoriteRepository.getAllFavorites(userId)
                    .collect { favorites ->
                        _favoriteQuotes.value = favorites
                    }
            } catch (e: Exception) {
                Log.e("Database", "Error fetching favorites: ${e.message}")
            } finally {
                quoteStateHolder.stopLoading()
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
        // TODO: 코루틴 도전
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val selectedCategory = selectedQuoteCategory.value ?: return
        val currentQuote = quotes.value.find { it.id == quoteId } ?: return

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

    private fun addFavoriteToFirestore(currentUser: FirebaseUser, quoteId: String) {

        val favoriteData = hashMapOf(
            "addedAt" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "quoteId" to quoteId,
            "category" to selectedQuoteCategory.value?.getLowercaseCategoryId()
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
            } catch (e: Exception) {
                Log.e("Favorite", "즐겨찾기 제거 실패", e)
            }
        }
    }
}