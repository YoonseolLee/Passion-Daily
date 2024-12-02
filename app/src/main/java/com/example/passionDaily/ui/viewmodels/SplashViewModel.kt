//package com.example.passionDaily.ui.viewmodels
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.passionDaily.auth.GoogleAuthClient
//import com.example.passionDaily.navigation.NavAction
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class SplashViewModel @Inject constructor(
//    private val googleAuthClient: GoogleAuthClient
//) : ViewModel() {
//    private val _navigationAction = MutableStateFlow<NavAction?>(null)
//    val navigationAction = _navigationAction.asStateFlow()
//
//    init {
//        checkLoginStatus()
//    }
//
//    private fun checkLoginStatus() {
//        viewModelScope.launch {
//            delay(3000L) // 3초 대기
//            _navigationAction.value = if (googleAuthClient.isSignedIn()) {
//                NavAction.NavigateToQuoteScreen
//            } else {
//                NavAction.NavigateToLoginScreen
//            }
//        }
//    }
//
//    fun clearNavigationAction() {
//        _navigationAction.value = null
//    }
//}