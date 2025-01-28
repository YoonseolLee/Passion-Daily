package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.os.NetworkOnMainThreadException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Quote.KEY_QUOTE_INDEX
import com.example.passionDaily.constants.ViewModelConstants.Quote.PAGE_SIZE
import com.example.passionDaily.constants.ViewModelConstants.Quote.TAG
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.manager.QuoteCategoryManager
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.usecase.IncrementShareCountUseCase
import com.example.passionDaily.usecase.LoadQuoteUseCase
import com.example.passionDaily.usecase.ShareQuoteUseCase
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val categoryManager: QuoteCategoryManager,
    private val loadQuoteUseCase: LoadQuoteUseCase,
    private val sharedQuoteUseCases: ShareQuoteUseCase,
    private val incrementShareCountUseCase: IncrementShareCountUseCase,
) : ViewModel(), QuoteInteractionHandler {

    private val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    private val quotes = quoteStateHolder.quotes
    private val isQuoteLoading = quoteStateHolder.isQuoteLoading
    private val hasQuoteReachedEnd = quoteStateHolder.hasQuoteReachedEnd
    private var lastLoadedQuote: DocumentSnapshot? = null

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(KEY_QUOTE_INDEX, 0)
    val currentQuote: StateFlow<Quote?> = combine(quotes, _currentQuoteIndex) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    init {
        viewModelScope.launch {
            launch {
                selectedQuoteCategory.value?.let { category ->
                    loadQuotes(category)
                }
            }
        }
    }

    fun navigateToQuoteWithCategory(quoteId: String, category: String) {
        viewModelScope.launch {
            try {
                startQuoteLoading()

                // 카테고리 설정
                val quoteCategory = initializeCategory(category) ?: return@launch

                val beforeQuotes = fetchQuotesBeforeTarget(quoteId, quoteCategory)
                val targetQuote =
                    fetchTargetQuote(quoteId, quoteCategory) ?: return@launch

                setupInitialQuoteDisplay(beforeQuotes, targetQuote)

                val result = fetchFurtherQuotes(quoteId, quoteCategory)
                lastLoadedQuote = updateLastLoadedDocument(result.lastDocument)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "Failed to load quotes", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to quote", e)
            } finally {
                stopIsQuoteLoading()
            }
        }
    }

    private suspend fun initializeCategory(category: String): QuoteCategory? {
        return categoryManager.setupCategory(category)
    }

    private suspend fun fetchQuotesBeforeTarget(
        quoteId: String,
        category: QuoteCategory
    ): List<Quote> {
        return loadQuoteUseCase.loadQuotesBeforeTarget(quoteId, category)
    }

    private suspend fun fetchTargetQuote(quoteId: String, category: QuoteCategory): Quote? {
        return loadQuoteUseCase.loadTargetQuote(quoteId, category)
    }

    private suspend fun fetchFurtherQuotes(
        quoteId: String,
        category: QuoteCategory
    ): RemoteQuoteRepositoryImpl.QuoteResult {
        return loadQuoteUseCase.loadFurtherQuotes(quoteId, category)
    }

    private fun updateLastLoadedDocument(document: DocumentSnapshot?): DocumentSnapshot? {
        return loadQuoteUseCase.getUpdatedLastLoadedQuote(document)
    }

    private fun updateQuoteIndex(index: Int): Int {
        return loadQuoteUseCase.getUpdatedQuoteIndex(index)
    }

    private suspend fun startQuoteLoading() {
        quoteStateHolder.updateIsQuoteLoading(true)
    }

    private suspend fun stopIsQuoteLoading() {
        quoteStateHolder.updateIsQuoteLoading(false)
    }

    private suspend fun setupInitialQuoteDisplay(beforeQuotes: List<Quote>, targetQuote: Quote) {
        loadQuoteUseCase.replaceQuotes(beforeQuotes, targetQuote)
        savedStateHandle[KEY_QUOTE_INDEX] = updateQuoteIndex(beforeQuotes.size)
    }

    fun loadInitialQuotes(category: QuoteCategory?) {
        viewModelScope.launch {
            try {
                startQuoteLoading()
                category?.let {
                    loadQuotes(it)
                }
            } catch (e: NetworkOnMainThreadException) {
                Log.e(TAG, "Network operation on main thread", e)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial quotes", e)
            }
        }
    }

    override fun previousQuote() {
        if (_currentQuoteIndex.value == 0 && hasQuoteReachedEnd.value) {
            savedStateHandle[KEY_QUOTE_INDEX] = quotes.value.size - 1
            return
        }

        if (_currentQuoteIndex.value == 0) {
            savedStateHandle[KEY_QUOTE_INDEX] = _currentQuoteIndex.value
            return
        }

        savedStateHandle[KEY_QUOTE_INDEX] = _currentQuoteIndex.value - 1
    }

    private fun loadQuotes(category: QuoteCategory) {
        if (isQuoteLoading.value) return

        viewModelScope.launch {
            try {
                startQuoteLoading()
                val result = fetchQuotesByCategory(
                    category = category,
                    pageSize = PAGE_SIZE,
                    lastLoadedQuote = lastLoadedQuote
                )

                if (result.quotes.isEmpty()) {
                    setHasQuoteReachedEndTrue()
                    return@launch
                }

                lastLoadedQuote = result.lastDocument
                addQuotesToState(
                    result.quotes,
                    isNewCategory = lastLoadedQuote == null
                )
            } catch (e: NetworkOnMainThreadException) {
                Log.e(TAG, "Network operation on main thread", e)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading quotes", e)
            } finally {
                quoteStateHolder.updateIsQuoteLoading(false)
            }
        }
    }

    private suspend fun fetchQuotesByCategory(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): RemoteQuoteRepositoryImpl.QuoteResult {
        return remoteQuoteRepository.getQuotesByCategory(
            category = category,
            pageSize = pageSize,
            lastLoadedQuote = lastLoadedQuote
        )
    }

    private suspend fun setHasQuoteReachedEndTrue() {
        quoteStateHolder.updateHasQuoteReachedEnd(true)
    }

    private suspend fun addQuotesToState(quotes: List<Quote>, isNewCategory: Boolean) {
        quoteStateHolder.addQuotes(quotes, isNewCategory)
    }

    override fun nextQuote() {
        val nextIndex = _currentQuoteIndex.value + 1
        val currentQuotes = quotes.value

        if (shouldLoadMoreQuotes(nextIndex, currentQuotes, hasQuoteReachedEnd)) {
            loadMoreQuotesIfNeeded()
            savedStateHandle[KEY_QUOTE_INDEX] = _currentQuoteIndex.value
            return
        }

        if (isLastQuote(nextIndex, currentQuotes)) {
            savedStateHandle[KEY_QUOTE_INDEX] = 0
            return
        }
        savedStateHandle[KEY_QUOTE_INDEX] = nextIndex
    }

    private fun shouldLoadMoreQuotes(
        nextIndex: Int,
        currentQuotes: List<Quote>,
        hasQuoteReachedEnd: StateFlow<Boolean>
    ): Boolean {
        return loadQuoteUseCase.shouldLoadMoreQuotes(nextIndex, currentQuotes, hasQuoteReachedEnd)
    }

    private fun isLastQuote(nextIndex: Int, currentQuotes: List<Quote>): Boolean {
        return loadQuoteUseCase.isLastQuote(nextIndex, currentQuotes)
    }

    private fun loadMoreQuotesIfNeeded() {
        if (loadQuoteUseCase.shouldLoadMoreQuotesIfNeeded(
                selectedCategory = selectedQuoteCategory.value,
                isQuoteLoading = isQuoteLoading.value,
                lastLoadedQuote = lastLoadedQuote
            )
        ) {
            selectedQuoteCategory.value?.let { category ->
                loadQuotesAfter(category, quotes.value.last().id)
            }
        }
    }

    private fun loadQuotesAfter(category: QuoteCategory, lastQuoteId: String) {
        viewModelScope.launch {
            try {
                val result = loadQuoteUseCase.loadQuotesAfter(
                    category = category,
                    lastQuoteId = lastQuoteId,
                    pageSize = PAGE_SIZE
                )
                loadQuoteUseCase.updateQuotesAfterLoading(result) { newLastDocument ->
                    lastLoadedQuote = newLastDocument
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading quotes: ${e.localizedMessage}")
            }
        }
    }

    fun shareQuote(context: Context, imageUrl: String?, quoteText: String, author: String) {
        viewModelScope.launch {
            try {
                sharedQuoteUseCases.shareQuote(
                    context = context,
                    imageUrl = imageUrl,
                    quoteText = quoteText,
                    author = author
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing quote", e)
            }
        }
    }

    fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        viewModelScope.launch {
            try {
                incrementShareCountUseCase.incrementShareCount(
                    quoteId = quoteId,
                    category = category
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing share count", e)
            }
        }
    }

    fun onCategorySelected(category: QuoteCategory?) {
        viewModelScope.launch {
            resetCategorySelection(category)
            category?.let { loadQuotes(it) }
        }
    }

    private suspend fun resetCategorySelection(category: QuoteCategory?) {
        quoteStateHolder.updateSelectedCategory(category)
        lastLoadedQuote = null
        savedStateHandle[KEY_QUOTE_INDEX] = 0
        clearQuotes()
    }

    private suspend fun clearQuotes() {
        quoteStateHolder.clearQuotes()
    }

    fun getStateHolder(): QuoteStateHolder {
        return quoteStateHolder
    }
}