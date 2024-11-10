package com.example.passionDaily.ui.viewmodels.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {
    protected val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    protected fun startLoading() {
        _isLoading.value = true
    }

    protected fun stopLoading() {
        _isLoading.value = false
    }
}