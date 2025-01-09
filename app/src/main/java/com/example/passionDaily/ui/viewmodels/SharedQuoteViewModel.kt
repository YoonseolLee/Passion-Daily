package com.example.passionDaily.ui.viewmodels


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.domain.usecase.QuoteUseCases
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedQuoteViewModel @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val quoteUseCases: QuoteUseCases,
    private val quoteStateHolder: QuoteStateHolder
) : ViewModel(), QuoteViewModelInterface {

    companion object {
        private var lastLoadedQuote: DocumentSnapshot? = null
        private val pageSize: Int = 20
    }

    private val quoteCategories = QuoteCategory.values().map { it.koreanName }

    val selectedQuoteCategory = quoteStateHolder.selectedQuoteCategory
    val quotes = quoteStateHolder.quotes

    private val _currentQuoteIndex = MutableStateFlow(0)
    val currentQuote: StateFlow<Quote?> = combine(quotes, _currentQuoteIndex) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isLoading = quoteStateHolder.isLoading

    private val _hasReachedEnd = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            launch {
                selectedQuoteCategory.value?.let { category ->
                    loadQuotes(category)
                }
            }
        }
    }

    override fun nextQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val nextIndex = currentIndex + 1

            when {
                nextIndex >= quotes.value.size && !_hasReachedEnd.value -> {
                    selectedQuoteCategory.value?.let { category ->
                        if (!isLoading.value && lastLoadedQuote != null) {
                            loadQuotes(category)
                        }
                    }
                    currentIndex
                }

                nextIndex >= quotes.value.size -> 0
                else -> nextIndex
            }
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            when {
                currentIndex == 0 && _hasReachedEnd.value -> quotes.value.size - 1
                currentIndex == 0 -> currentIndex
                else -> currentIndex - 1
            }
        }
    }

    fun loadQuotes(category: QuoteCategory) {
        if (isLoading.value) return

        viewModelScope.launch {
            quoteStateHolder.startLoading()

            try {
                val result = remoteQuoteRepository.getQuotesByCategory(
                    category = category,
                    pageSize = pageSize,
                    lastLoadedQuote = lastLoadedQuote
                )

                if (result.quotes.isNotEmpty()) {
                    lastLoadedQuote = result.lastDocument
                    quoteStateHolder.addQuotes(
                        result.quotes,
                        isNewCategory = lastLoadedQuote == null
                    )
                } else {
                    _hasReachedEnd.value = true
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching quotes: ${e.message}")
            } finally {
                quoteStateHolder.stopLoading()
            }
        }
    }

    fun getQuoteCategories(): List<String> {
        return quoteCategories
    }

    fun shareText(context: Context, text: String) {
        quoteUseCases.shareText(context, text)
    }

    fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        category?.let {
            viewModelScope.launch {
                try {
                    remoteQuoteRepository.incrementShareCount(quoteId, it)
                } catch (e: Exception) {
                    Log.e("ShareCount", "Error incrementing share count", e)
                }
            }
        } ?: run {
            Log.e("ShareCount", "Category is nultoQuotel")
        }
    }

    fun onCategorySelected(category: QuoteCategory?) {
        quoteStateHolder.updateSelectedCategory(category)
        lastLoadedQuote = null  // 페이지네이션 상태 초기화
        _currentQuoteIndex.value = 0  // 현재 인덱스도 초기화
        quoteStateHolder.clearQuotes()  // 기존 quotes 초기화
        category?.let { loadQuotes(it) }
    }
}