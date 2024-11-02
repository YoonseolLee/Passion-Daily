package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoadingViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun startLoading() {
        _isLoading.value = true
    }

    fun stopLoading() {
        _isLoading.value = false
    }
}
