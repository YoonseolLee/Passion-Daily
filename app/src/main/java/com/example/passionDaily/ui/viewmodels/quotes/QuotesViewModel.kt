package com.example.passionDaily.ui.viewmodels.quotes

import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.entity.QuoteEntity
import com.example.passionDaily.data.repository.PassionDailyRepository
import com.example.passionDaily.ui.viewmodels.base.BaseViewModel
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val repository: PassionDailyRepository
) : BaseViewModel() {
    private val _quotes = MutableStateFlow<RequestState<List<QuoteEntity>>>(RequestState.Idle)
    val quotes: StateFlow<RequestState<List<QuoteEntity>>> = _quotes.asStateFlow()

    private val _categories =
        MutableStateFlow<RequestState<List<QuoteCategoryEntity>>>(RequestState.Idle)
    val categories: StateFlow<RequestState<List<QuoteCategoryEntity>>> = _categories.asStateFlow()

    init {
        loadQuotes()
        loadCategories()
    }

    private fun loadQuotes() {
        viewModelScope.launch {
            startLoading()
            _quotes.value = RequestState.Loading
            try {
                repository.getAllQuotes().collect { quotesList ->
                    _quotes.value = RequestState.Success(quotesList)
                    stopLoading()
                }
            } catch (e: Exception) {
                _quotes.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = RequestState.Loading
            try {
                val categoriesList = repository.getAllCategories()
                _categories.value = RequestState.Success(categoriesList)
            } catch (e: Exception) {
                _categories.value = RequestState.Error(e)
            }
        }
    }
}