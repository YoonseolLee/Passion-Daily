package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.passionDaily.util.Categories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor() : ViewModel() {
    private val categories = Categories.values().map { it.toKorean() }

    fun getCategories(): List<String> {
        return categories
    }
}