package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.os.NetworkOnMainThreadException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.constants.ViewModelConstants.Quote.KEY_QUOTE_INDEX
import com.example.passionDaily.constants.ViewModelConstants.Quote.PAGE_SIZE
import com.example.passionDaily.constants.ViewModelConstants.Quote.TAG
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.manager.QuoteCategoryManager
import com.example.passionDaily.quote.action.QuoteViewModelActions
import com.example.passionDaily.quote.state.QuoteStateHolder
import com.example.passionDaily.quote.state.QuoteViewModelState
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.usecase.IncrementShareCountUseCase
import com.example.passionDaily.usecase.LoadQuoteUseCase
import com.example.passionDaily.usecase.ShareQuoteUseCase
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val categoryManager: QuoteCategoryManager,
    private val loadQuoteUseCase: LoadQuoteUseCase,
    private val sharedQuoteUseCases: ShareQuoteUseCase,
    private val incrementShareCountUseCase: IncrementShareCountUseCase,
    private val exceptionHandler: CoroutineExceptionHandler,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val stringProvider: StringProvider,
) : ViewModel(), QuoteViewModelState, QuoteViewModelActions {
    private var lastLoadedQuote: DocumentSnapshot? = null
    override val quotes: StateFlow<List<Quote>> = quoteStateHolder.quotes
    override val isLoading: StateFlow<Boolean> = quoteStateHolder.isQuoteLoading
    override val hasReachedEnd: StateFlow<Boolean> = quoteStateHolder.hasQuoteReachedEnd
    override val selectedCategory: StateFlow<QuoteCategory?> = quoteStateHolder.selectedQuoteCategory

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(KEY_QUOTE_INDEX, 0)
    override val currentQuoteIndex: StateFlow<Int> = _currentQuoteIndex

    override val currentQuote: StateFlow<Quote?> =
        combine(quotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    init {
        viewModelScope.launch(exceptionHandler) {
            selectedCategory.value?.let { category ->
                loadQuotes(category)
            }
        }
    }

    override fun navigateToQuoteWithCategory(quoteId: String, category: String) {
        viewModelScope.launch(exceptionHandler + defaultDispatcher) {
            try {
                coroutineScope {
                    withContext(Dispatchers.Main) {
                        startQuoteLoading()
                    }
                }

                /**
                 *  return@coroutineScope는 이 coroutineScope 블록만 종료
                 *  바깥의 launch는 계속 실행됨 (finally 블록으로 이동)
                 */
                run {
                    // 1. 카테고리 설정
                    val quoteCategory = initializeCategory(category) ?: return@run

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
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, stringProvider.getString(R.string.error_quote_not_found))
            } finally {
                withContext(Dispatchers.Main) {
                    stopIsQuoteLoading()
                }
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

    override fun loadInitialQuotes(category: QuoteCategory?) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    startQuoteLoading()
                }
                category?.let {
                    loadQuotes(it)
                }
            } catch (e: NetworkOnMainThreadException) {
                Log.e(TAG, stringProvider.getString(R.string.error_network_main_thread), e)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, stringProvider.getString(R.string.error_firebase_firestore), e)
            } catch (e: Exception) {
                Log.e(TAG, stringProvider.getString(R.string.error_quote_not_found), e)
            }
        }
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

    override fun loadQuotes(category: QuoteCategory) {
        if (isLoading.value) return

        viewModelScope.launch(exceptionHandler + defaultDispatcher) {
            try {
                coroutineScope {
                    withContext(Dispatchers.Main) {
                        startQuoteLoading()
                    }
                }
                run {
                    val result = withTimeout(10_000L) {
                        fetchQuotesByCategory(
                            category = category,
                            pageSize = PAGE_SIZE,
                            lastLoadedQuote = lastLoadedQuote
                        )
                    }

                    if (result.quotes.isEmpty()) {
                        setHasQuoteReachedEndTrue()
                        return@run
                    }

                    lastLoadedQuote = result.lastDocument
                    addQuotesToState(
                        result.quotes,
                        isNewCategory = lastLoadedQuote == null
                    )
                }
            } catch (e: NetworkOnMainThreadException) {
                Log.e(TAG, stringProvider.getString(R.string.error_network_main_thread), e)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, stringProvider.getString(R.string.error_firebase_firestore), e)
            } catch (e: Exception) {
                Log.e(TAG, stringProvider.getString(R.string.error_quote_not_found), e)
            } finally {
                withContext(Dispatchers.Main) {
                    quoteStateHolder.updateIsQuoteLoading(false)
                }
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

        if (shouldLoadMoreQuotes(nextIndex, currentQuotes, hasReachedEnd)) {
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
                selectedCategory = selectedCategory.value,
                isQuoteLoading = isLoading.value,
                lastLoadedQuote = lastLoadedQuote
            )
        ) {
            selectedCategory.value?.let { category ->
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
            } catch (e: NetworkOnMainThreadException) {
                Log.e(TAG, stringProvider.getString(R.string.error_network_main_thread), e)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, stringProvider.getString(R.string.error_firebase_firestore), e)
            } catch (e: Exception) {
                Log.e(TAG, stringProvider.getString(R.string.error_quote_not_found), e)
            }
        }
    }

    override fun shareQuote(context: Context, imageUrl: String?, quoteText: String, author: String) {
        viewModelScope.launch(exceptionHandler + defaultDispatcher) {
            try {
                sharedQuoteUseCases.shareQuote(
                    context = context,
                    imageUrl = imageUrl,
                    quoteText = quoteText,
                    author = author
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, stringProvider.getString(R.string.error_sharing_quote), e)
            }
        }
    }

    override fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        viewModelScope.launch(exceptionHandler + defaultDispatcher) {
            supervisorScope {
                try {
                    incrementShareCountUseCase.incrementShareCount(
                        quoteId = quoteId,
                        category = category
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        stringProvider.getString(R.string.error_share_count_increment_fail),
                        e
                    )
                }
            }
        }
    }

    override fun onCategorySelected(category: QuoteCategory?) {
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