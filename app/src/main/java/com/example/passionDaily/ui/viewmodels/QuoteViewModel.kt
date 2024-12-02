package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.local.relation.QuoteWithCategory
import com.example.passionDaily.data.repository.local.QuoteRepository
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _quotes = MutableStateFlow<RequestState<List<QuoteEntity>>>(RequestState.Idle)
    val quotes: StateFlow<RequestState<List<QuoteEntity>>> = _quotes

    private val _selectedQuote = MutableStateFlow<QuoteWithCategory?>(null)
    val selectedQuote: StateFlow<QuoteWithCategory?> = _selectedQuote

    fun getQuotesByCategory(categoryId: Int) {
        _quotes.value = RequestState.Loading
        viewModelScope.launch {
            try {
                quoteRepository.getQuotesByCategory(categoryId).collect { quotes ->
                    _quotes.value = RequestState.Success(quotes)
                }
            } catch (e: Exception) {
                _quotes.value = RequestState.Error(e)
            }
        }
    }

    fun getQuoteWithCategory(quoteId: Int) {
        viewModelScope.launch {
            try {
                val quote = quoteRepository.getQuoteWithCategory(quoteId)
                _selectedQuote.value = quote
            } catch (e:Exception) {
                // Handle Error
            }
        }
    }
}
