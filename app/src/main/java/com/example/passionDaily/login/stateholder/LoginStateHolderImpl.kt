package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginStateHolderImpl @Inject constructor() : LoginStateHolder {

    // 로그인 상태
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 사용자 프로필 JSON 데이터
    private val _userProfileJson = MutableStateFlow<String?>(null)
    override val userProfileJson: StateFlow<String?> = _userProfileJson.asStateFlow()

    // 사용자 프로필 JSON 데이터 (V2)
    private val _userProfileJsonV2 = MutableStateFlow<String?>(null)
    override val userProfileJsonV2: StateFlow<String?> = _userProfileJsonV2.asStateFlow()

    // 로그인 여부
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override suspend fun updateAuthState(authState: AuthState) {
        _authState.emit(authState)
    }

    override suspend fun updateUserProfileJson(json: String?) {
        _userProfileJson.emit(json)
    }

    override suspend fun updateUserProfileJsonV2(json: String?) {
        _userProfileJsonV2.emit(json)
    }

    override suspend fun updateIsLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.emit(isLoggedIn)
    }

    override suspend fun updateIsLoading(isLoading: Boolean) {
        _isLoading.emit(isLoading)
    }
}