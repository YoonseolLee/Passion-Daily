package com.example.passionDaily.ui.viewmodels.splash

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passionDaily.ui.viewmodels.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel() {
    init {
        // 임시 코드임.. 나중에 실제 데이터 로딩이 완료되는 걸로 바꿔야함!!
        viewModelScope.launch {
            startLoading()
            delay(2000)
            stopLoading()
        }
    }
}