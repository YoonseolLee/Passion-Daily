package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.repository.local.QuoteCategoryRepository
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteCategoryViewModel @Inject constructor(
    private val quoteCategoryRepository: QuoteCategoryRepository
) : ViewModel() {
    private val _categories =
        MutableStateFlow<RequestState<List<QuoteCategoryEntity>>>(RequestState.Idle)
    val categories: StateFlow<RequestState<List<QuoteCategoryEntity>>> = _categories

    private val _selectedCategory = MutableStateFlow<QuoteCategoryEntity?>(null)
    val selectedCategory: StateFlow<QuoteCategoryEntity?> = _selectedCategory

    init {
        getAllCategories()
    }

    private fun getAllCategories() {
        _categories.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val categoryList = quoteCategoryRepository.getAllCategories()
                _categories.value = RequestState.Success(categoryList)
            } catch (e: Exception) {
                _categories.value = RequestState.Error(e)
            }
        }
    }

    fun selectCategory(categoryId: Int) {
        viewModelScope.launch {
            try {
                val category = quoteCategoryRepository.getCategoryById(categoryId)
                _selectedCategory.value = category
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}