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
import com.example.passionDaily.domain.usecase.QuoteUseCase
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val quoteUseCase: QuoteUseCase,
    private val quoteStateHolder: QuoteStateHolder,
    private val savedStateHandle: SavedStateHandle,
    private val stringProvider: StringProvider,

) : ViewModel(), QuoteInteractionHandler {

    companion object {
        private const val TAG = "QuoteViewModel"
        private const val PAGE_SIZE = 20
        private const val KEY_QUOTE_INDEX = "quote_index"
    }

    private var lastLoadedQuote: DocumentSnapshot? = null
    private val quoteCategories = QuoteCategory.values().map { it.koreanName }

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
        savedStateHandle[KEY_QUOTE_INDEX] = when {
            nextIndex >= quotes.value.size && !_hasQuoteReachedEnd.value -> {
                selectedQuoteCategory.value?.let { category ->
                    if (!_isQuoteLoading.value && lastLoadedQuote != null) {
                        loadQuotes(category)
                    }
                }
                _currentQuoteIndex.value
            }
            nextIndex >= quotes.value.size -> 0
            else -> nextIndex
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

    fun shareText(context: Context, text: String) {
        quoteUseCase.shareText(context, text)
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

    fun getQuoteCategories(): List<String> = quoteCategories

}