package com.example.passionDaily.ui.viewmodels.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.repository.QuoteCategoryRepository
import com.example.passionDaily.data.repository.QuoteRepository
import com.example.passionDaily.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val categoryRepository: QuoteCategoryRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _recentCategories = MutableStateFlow<List<QuoteCategoryEntity>>(emptyList())
    val recentCategories: StateFlow<List<QuoteCategoryEntity>> = _recentCategories.asStateFlow()

    private val splashDelay = 2000L  // 2초 동안 스플래시 화면을 유지

//    init {
//        checkLoginStatus()
//    }
//
//    private fun checkLoginStatus() {
//        viewModelScope.launch {
//            delay(splashDelay)
//            _isLoggedIn.value = userRepository.hasLoginHistory() // 로그인 이력 확인
//
//            if (_isLoggedIn.value) {
//                loadRecentCategories()
//            }
//        }
//    }
//
//    private suspend fun loadRecentCategories() {
//        // 최근 선택한 카테고리 로드
//        _recentCategories.value = categoryRepository.getRecentCategories()
//    }
}