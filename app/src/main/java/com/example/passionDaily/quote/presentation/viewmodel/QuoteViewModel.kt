package com.example.passionDaily.quote.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Quote.KEY_QUOTE_INDEX
import com.example.passionDaily.constants.ViewModelConstants.Quote.PAGE_SIZE
import com.example.passionDaily.quote.base.QuoteViewModelActions
import com.example.passionDaily.quote.base.QuoteViewModelState
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.manager.QuoteLoadingManager
import com.example.passionDaily.quote.manager.ShareQuoteManager
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManager
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.toast.manager.ToastManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val categoryManager: QuoteCategoryManager,
    private val toastManager: ToastManager,
    private val quoteLoadingManager: QuoteLoadingManager,
    private val shareQuoteManager: ShareQuoteManager,
) : ViewModel(), QuoteViewModelState, QuoteViewModelActions {
    private var lastLoadedQuote: DocumentSnapshot? = null
    override val quotes: StateFlow<List<Quote>> = quoteStateHolder.quotes
    override val isLoading: StateFlow<Boolean> = quoteStateHolder.isQuoteLoading
    override val hasReachedEnd: StateFlow<Boolean> = quoteStateHolder.hasQuoteReachedEnd
    override val selectedCategory: StateFlow<QuoteCategory> =
        quoteStateHolder.selectedQuoteCategory

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(KEY_QUOTE_INDEX, 0)
    override val currentQuoteIndex: StateFlow<Int> = _currentQuoteIndex

    override val currentQuote: StateFlow<Quote?> =
        combine(quotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            selectedCategory.value?.let { category ->
                try {
                    quoteStateHolder.updateIsQuoteLoading(true)
                    val result = quoteLoadingManager.fetchQuotesByCategory(
                        category = category,
                        pageSize = PAGE_SIZE,
                        lastLoadedQuote = null
                    )

                    if (result.quotes.isNotEmpty()) {
                        lastLoadedQuote = result.lastDocument
                        quoteLoadingManager.addQuotesToState(
                            result.quotes,
                            isNewCategory = true
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    quoteStateHolder.updateIsQuoteLoading(false)
                }
            }
        }
    }

    override fun loadQuotes(category: QuoteCategory) {
        viewModelScope.launch {
            try {
                quoteStateHolder.updateIsQuoteLoading(true)

                val result = quoteLoadingManager.fetchQuotesByCategory(
                    category = category,
                    pageSize = PAGE_SIZE,
                    lastLoadedQuote = lastLoadedQuote
                )

                if (result.quotes.isEmpty()) {
                    quoteLoadingManager.setHasQuoteReachedEndTrue()
                    return@launch
                }

                // quotes 추가 전에 상태 확인
                if (quotes.value.isEmpty()) {
                    lastLoadedQuote = result.lastDocument
                    quoteLoadingManager.addQuotesToState(result.quotes, true)
                } else {
                    lastLoadedQuote = result.lastDocument
                    quoteLoadingManager.addQuotesToState(result.quotes, false)
                }

            } catch (e: Exception) {
                handleError(e)
            } finally {
                quoteStateHolder.updateIsQuoteLoading(false)
            }
        }
    }

    override fun navigateToQuoteWithCategory(quoteId: String, category: String) {
        viewModelScope.launch {
            try {
                quoteLoadingManager.startQuoteLoading()
                run {
                    // 1. 카테고리 설정
                    val quoteCategory = categoryManager.setupCategory(category) ?: return@run

                    // 2. 순차적 데이터 로딩
                    val beforeQuotes = fetchQuotesBeforeTarget(quoteId, quoteCategory)
                    val targetQuote = fetchTargetQuote(quoteId, quoteCategory) ?: return@run

                    // 3. UI 상태 업데이트
                    withContext(Dispatchers.Main) {
                        setupInitialQuoteDisplay(beforeQuotes, targetQuote)
                    }

                    // 4. 추가 데이터 로딩
                    val result = fetchFurtherQuotes(quoteId, quoteCategory)
                    lastLoadedQuote = updateLastLoadedDocument(result.lastDocument)

                    if (result.quotes.isEmpty()) {
                        quoteLoadingManager.setHasQuoteReachedEndTrue()
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                stopIsQuoteLoading()
            }
        }
    }

    private suspend fun fetchQuotesBeforeTarget(
        quoteId: String,
        category: QuoteCategory
    ): List<Quote> {
        return quoteLoadingManager.loadQuotesBeforeTarget(quoteId, category)
    }

    private suspend fun fetchTargetQuote(quoteId: String, category: QuoteCategory): Quote? {
        return quoteLoadingManager.loadTargetQuote(quoteId, category)
    }

    private suspend fun setupInitialQuoteDisplay(beforeQuotes: List<Quote>, targetQuote: Quote) {
        quoteLoadingManager.replaceQuotes(beforeQuotes, targetQuote)
        savedStateHandle[KEY_QUOTE_INDEX] = updateQuoteIndex(beforeQuotes.size)
    }

    private fun updateQuoteIndex(index: Int): Int {
        return quoteLoadingManager.getUpdatedQuoteIndex(index)
    }

    private suspend fun fetchFurtherQuotes(
        quoteId: String,
        category: QuoteCategory
    ): QuoteResult {
        return quoteLoadingManager.loadFurtherQuotes(quoteId, category)
    }

    private fun updateLastLoadedDocument(document: DocumentSnapshot?): DocumentSnapshot? {
        return quoteLoadingManager.getUpdatedLastLoadedQuote(document)
    }

    private suspend fun stopIsQuoteLoading() {
        quoteStateHolder.updateIsQuoteLoading(false)
    }

    override fun previousQuote() {
        if (_currentQuoteIndex.value == 0 && hasReachedEnd.value) {
            savedStateHandle[KEY_QUOTE_INDEX] = quotes.value.size - 1
            return
        }

        if (_currentQuoteIndex.value == 0) {
            savedStateHandle[KEY_QUOTE_INDEX] = _currentQuoteIndex.value
            return
        }

        savedStateHandle[KEY_QUOTE_INDEX] = _currentQuoteIndex.value - 1
    }

    override fun nextQuote() {
        val nextIndex = _currentQuoteIndex.value + 1
        val currentQuotes = quotes.value

        if (shouldLoadMoreQuotes(nextIndex, currentQuotes, hasReachedEnd)) {
            viewModelScope.launch {
                try {
                    quoteLoadingManager.startQuoteLoading()

                    val result = withTimeout(10_000L) {
                        selectedCategory.value?.let { category ->
                            quoteLoadingManager.loadQuotesAfter(
                                category = category,
                                lastQuoteId = quotes.value.last().id,
                                pageSize = PAGE_SIZE
                            )
                        }
                    }

                    if (result == null || result.quotes.isEmpty()) {
                        quoteLoadingManager.setHasQuoteReachedEndTrue()
                        savedStateHandle[KEY_QUOTE_INDEX] = 0
                        return@launch
                    }

                    quoteLoadingManager.updateQuotesAfterLoading(result) { newLastDocument ->
                        lastLoadedQuote = newLastDocument
                    }

                    savedStateHandle[KEY_QUOTE_INDEX] = nextIndex

                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    quoteStateHolder.updateIsQuoteLoading(false)
                }
            }
            return
        }

        savedStateHandle[KEY_QUOTE_INDEX] = if (isLastQuote(nextIndex, currentQuotes)) 0 else nextIndex
    }

    private fun shouldLoadMoreQuotes(
        nextIndex: Int,
        currentQuotes: List<Quote>,
        hasQuoteReachedEnd: StateFlow<Boolean>
    ): Boolean {
        return quoteLoadingManager.shouldLoadMoreQuotes(
            nextIndex,
            currentQuotes,
            hasQuoteReachedEnd
        )
    }

    private fun isLastQuote(nextIndex: Int, currentQuotes: List<Quote>): Boolean {
        return quoteLoadingManager.isLastQuote(nextIndex, currentQuotes)
    }

    override fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) {
        viewModelScope.launch {
            try {
                shareQuoteManager.shareQuote(
                    context = context,
                    imageUrl = imageUrl,
                    quoteText = quoteText,
                    author = author
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    override fun incrementShareCount(quoteId: String, category: QuoteCategory) {
        viewModelScope.launch {
            supervisorScope {
                try {
                    shareQuoteManager.incrementShareCount(
                        quoteId = quoteId,
                        category = category
                    )
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    private var categoryChangeJob: Job? = null

    override fun onCategorySelected(category: QuoteCategory) {
        // 이전 Job 취소
        categoryChangeJob?.cancel()

        categoryChangeJob = viewModelScope.launch {
            try {
                // 모든 상태 초기화
                resetAllStates(category)

                // 새로운 데이터 로드
                loadQuotes(category)

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun resetAllStates(newCategory: QuoteCategory) {
        // 순서 보장을 위해 순차적으로 실행
        quoteStateHolder.updateIsQuoteLoading(false)
        quoteStateHolder.updateHasQuoteReachedEnd(false)
        quoteStateHolder.updateSelectedCategory(newCategory)
        quoteStateHolder.clearQuotes()

        lastLoadedQuote = null
        savedStateHandle[KEY_QUOTE_INDEX] = 0

    }

    fun getStateHolder(): QuoteStateHolder {
        return quoteStateHolder
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
            else -> {
                toastManager.showGeneralErrorToast()
            }
        }
    }
}