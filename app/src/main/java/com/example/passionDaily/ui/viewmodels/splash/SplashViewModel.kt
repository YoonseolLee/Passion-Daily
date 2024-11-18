package com.example.passionDaily.ui.viewmodels.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.repository.local.QuoteCategoryRepository
import com.example.passionDaily.data.repository.local.QuoteRepository
import com.example.passionDaily.data.repository.local.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _recentCategories = MutableStateFlow<List<QuoteCategoryEntity>>(emptyList())
    val recentCategories: StateFlow<List<QuoteCategoryEntity>> = _recentCategories.asStateFlow()

    init {
        // 스플래시 화면을 계속 표시하기 위해 isLoading을 true로 유지
        viewModelScope.launch {
            _isLoading.value = true
//            checkLoginStatus()
        }
    }

//    private fun checkLoginStatus() {
//        viewModelScope.launch {
//            try {
//                _isLoggedIn.value = userRepository.hasLoginHistory()
//                if (_isLoggedIn.value) {
//                    loadRecentCategories()
//                }
//            } catch (e: Exception) {
//                // 에러 처리
//            }
//        }
//    }
//
//    private suspend fun loadRecentCategories() {
//        try {
//            _recentCategories.value = categoryRepository.getRecentCategories()
//        } catch (e: Exception) {
//            // 에러 처리
//        }
//    }
}