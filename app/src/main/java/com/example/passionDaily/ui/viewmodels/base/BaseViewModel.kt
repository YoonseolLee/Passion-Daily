package com.example.passionDaily.ui.viewmodels.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    protected fun startLoading() {
        _isLoading.value = true
    }

    protected fun stopLoading() {
        _isLoading.value = false
    }

    // TODO: 뷰모델 생성
}
