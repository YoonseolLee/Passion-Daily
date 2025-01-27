package com.example.passionDaily.ui.viewmodels


import android.content.Context
import android.os.NetworkOnMainThreadException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.manager.ImageShareManager
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val imageShareManager: ImageShareManager,
    private val stringProvider: StringProvider,
) : ViewModel(), QuoteInteractionHandler {

    companion object {
        private const val TAG = "QuoteViewModel"
        private const val PAGE_SIZE = 20
        private const val KEY_QUOTE_INDEX = "quote_index"
    }

    private var lastLoadedQuote: DocumentSnapshot? = null
    private val _categories = MutableStateFlow(QuoteCategory.values().map { it.koreanName })
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    val quotes = quoteStateHolder.quotes

    private val _currentQuoteIndex = savedStateHandle.getStateFlow(KEY_QUOTE_INDEX, 0)
    val currentQuote: StateFlow<Quote?> = combine(quotes, _currentQuoteIndex) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _isQuoteLoading = MutableStateFlow(false)
    val isQuoteLoading: StateFlow<Boolean> = _isQuoteLoading.asStateFlow()

    private val _hasQuoteReachedEnd = MutableStateFlow(false)

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
                _isQuoteLoading.emit(true)

                // 1. 카테고리 처리
                val quoteCategory = findCategory(category) ?: return@launch
                updateQuoteCategory(quoteCategory)

                // 2. 명언 데이터 로드
                val beforeQuotes = loadQuotesBeforeTarget(quoteId, quoteCategory)
                val targetQuote = loadTargetQuote(quoteId, quoteCategory) ?: return@launch

                // 3. 명언 데이터 설정
                clearExistingQuotes()
                addInitialQuotes(beforeQuotes, targetQuote)
                updateQuoteIndex(beforeQuotes.size)

                // 4. 추가 명언 로드
                val afterQuotesResult = loadQuotesAfterTarget(quoteId, quoteCategory)
                addAfterQuotes(afterQuotesResult)

            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to quote", e)
            } finally {
                _isQuoteLoading.emit(false)
            }
        }
    }

    private fun findCategory(category: String): QuoteCategory? {
        return QuoteCategory.values()
            .find { it.name.lowercase() == category.lowercase() }
            .also {
                if (it == null) Log.w(TAG, "Invalid category: $category")
            }
    }

    private suspend fun updateQuoteCategory(category: QuoteCategory) {
        quoteStateHolder.updateSelectedCategory(category)
    }

    private suspend fun loadQuotesBeforeTarget(
        quoteId: String,
        category: QuoteCategory
    ): List<Quote> {
        return remoteQuoteRepository.getQuotesBeforeId(
            category = category,
            targetQuoteId = quoteId,
            limit = PAGE_SIZE
        ).also { quotes ->
            Log.d(TAG, "Quotes before target: ${quotes.map { it.id }}")
        }
    }

    private suspend fun loadTargetQuote(
        quoteId: String,
        category: QuoteCategory
    ): Quote? {
        return remoteQuoteRepository.getQuoteById(quoteId, category)
            ?.also { quote ->
                Log.d(TAG, "Target quote: ${quote.id}")
            } ?: run {
            Log.w(TAG, "Target quote not found: $quoteId")
            null
        }
    }

    private suspend fun clearExistingQuotes() {
        quoteStateHolder.clearQuotes()
    }

    private suspend fun addInitialQuotes(beforeQuotes: List<Quote>, targetQuote: Quote) {
        val allQuotes = beforeQuotes + targetQuote
        quoteStateHolder.addQuotes(allQuotes, true)
    }

    private fun updateQuoteIndex(index: Int) {
        savedStateHandle[KEY_QUOTE_INDEX] = index
    }

    private suspend fun loadQuotesAfterTarget(
        quoteId: String,
        category: QuoteCategory
    ): RemoteQuoteRepositoryImpl.QuoteResult {
        return remoteQuoteRepository.getQuotesAfterId(
            category = category,
            afterQuoteId = quoteId,
            limit = PAGE_SIZE
        ).also { result ->
            Log.d(TAG, "Quotes after target: ${result.quotes.map { it.id }}")
        }
    }

    private suspend fun addAfterQuotes(afterQuotesResult: RemoteQuoteRepositoryImpl.QuoteResult) {
        if (afterQuotesResult.quotes.isNotEmpty()) {
            quoteStateHolder.addQuotes(afterQuotesResult.quotes, false)
            lastLoadedQuote = afterQuotesResult.lastDocument
        }
    }

    fun loadInitialQuotes(category: QuoteCategory?) {
        viewModelScope.launch {
            _isQuoteLoading.emit(true)
            category?.let {
                safeQuoteOperation { loadQuotes(it) }
            }
            _isQuoteLoading.emit(false)
        }
    }

    override fun previousQuote() {
        savedStateHandle[KEY_QUOTE_INDEX] = when {
            _currentQuoteIndex.value == 0 && _hasQuoteReachedEnd.value -> quotes.value.size - 1
            _currentQuoteIndex.value == 0 -> _currentQuoteIndex.value
            else -> _currentQuoteIndex.value - 1
        }
    }

    private fun loadQuotes(category: QuoteCategory) {
        if (_isQuoteLoading.value) return

        viewModelScope.launch {
            _isQuoteLoading.emit(true)
            safeQuoteOperation {
                val result = remoteQuoteRepository.getQuotesByCategory(
                    category = category,
                    pageSize = PAGE_SIZE,
                    lastLoadedQuote = lastLoadedQuote
                )

                if (result.quotes.isNotEmpty()) {
                    lastLoadedQuote = result.lastDocument
                    quoteStateHolder.addQuotes(
                        result.quotes,
                        isNewCategory = lastLoadedQuote == null
                    )
                } else {
                    _hasQuoteReachedEnd.emit(true)
                }
            }
            _isQuoteLoading.emit(false)
        }
    }

    override fun nextQuote() {
        val nextIndex = _currentQuoteIndex.value + 1
        val currentQuotes = quotes.value

        // 추가 명언을 로드해야 하는 경우 (현재 페이지의 마지막 명언에 도달했을 때)
        if (shouldLoadMoreQuotes(nextIndex, currentQuotes)) {
            loadMoreQuotesIfNeeded()
            // 새로운 명언들이 로드되는 동안 현재 인덱스를 유지
            savedStateHandle[KEY_QUOTE_INDEX] = _currentQuoteIndex.value
            return
        }

        // 모든 명언의 마지막에 도달한 경우 첫 번째 명언으로 순환
        if (isLastQuote(nextIndex, currentQuotes)) {
            savedStateHandle[KEY_QUOTE_INDEX] = 0
            return
        }

        // 일반적인 경우: 다음 명언으로 이동
        savedStateHandle[KEY_QUOTE_INDEX] = nextIndex
    }

    private fun shouldLoadMoreQuotes(nextIndex: Int, currentQuotes: List<Quote>): Boolean {
        return nextIndex >= currentQuotes.size && !_hasQuoteReachedEnd.value
    }

    private fun isLastQuote(nextIndex: Int, currentQuotes: List<Quote>): Boolean {
        return nextIndex >= currentQuotes.size
    }

    private fun loadMoreQuotesIfNeeded() {
        selectedQuoteCategory.value?.let { category ->
            if (!_isQuoteLoading.value && lastLoadedQuote != null) {
                loadQuotesAfter(category, quotes.value.last().id)
            }
        }
    }

    private fun loadQuotesAfter(category: QuoteCategory, lastQuoteId: String) {
        viewModelScope.launch {
            val result = remoteQuoteRepository.getQuotesAfterId(
                category = category,
                afterQuoteId = lastQuoteId,
                limit = PAGE_SIZE
            )
            if (result.quotes.isNotEmpty()) {
                quoteStateHolder.addQuotes(result.quotes, false)
                lastLoadedQuote = result.lastDocument
            } else {
                _hasQuoteReachedEnd.emit(true)
            }
        }
    }

    private suspend fun safeQuoteOperation(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            val errorMessage = mapExceptionToErrorMessage(e)
            Log.e(TAG, "Error in quote operation", e)
        }
    }

    private fun mapExceptionToErrorMessage(e: Exception): String {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    stringProvider.getString(R.string.error_network)
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    stringProvider.getString(R.string.error_permission_denied)
                else ->
                    stringProvider.getString(R.string.error_firebase_firestore)
            }
            is NetworkOnMainThreadException ->
                stringProvider.getString(R.string.error_network_main_thread)
            else ->
                stringProvider.getString(R.string.error_unexpected, e.message.orEmpty())
        }
    }

    fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) {
        viewModelScope.launch {
            try {
                val imageShareManager = ImageShareManager(context)
                withContext(Dispatchers.Main) {
                    imageShareManager.shareQuoteImage(
                        context = context,
                        imageUrl = imageUrl,
                        quoteText = quoteText,
                        author = author
                    )
                }
            } catch (e: Exception) {
                Log.e("QuoteViewModel", "Error preparing and sharing quote", e)
            }
        }
    }

    fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        category?.let {
            viewModelScope.launch {
                safeQuoteOperation {
                    remoteQuoteRepository.incrementShareCount(quoteId, it)
                }
            }
        } ?: run {
            Log.e(TAG, "Category is null")
        }
    }

    fun onCategorySelected(category: QuoteCategory?) {
        quoteStateHolder.updateSelectedCategory(category)
        lastLoadedQuote = null
        savedStateHandle[KEY_QUOTE_INDEX] = 0
        quoteStateHolder.clearQuotes()
        category?.let { loadQuotes(it) }
    }
}