package com.example.passionDaily.ui.viewmodels.user

import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.repository.PassionDailyRepository
import com.example.passionDaily.ui.viewmodels.base.BaseViewModel
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: PassionDailyRepository
) : BaseViewModel() {
    private val _currentUser = MutableStateFlow<RequestState<UserEntity>>(RequestState.Idle)
    val currentUser: StateFlow<RequestState<UserEntity>> = _currentUser.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    fun setCurrentUser(userId: Int) {
        _currentUserId.value = userId
        loadUserData(userId)
    }

    private fun loadUserData(userId: Int) {
        viewModelScope.launch {
            startLoading()
            _currentUser.value = RequestState.Loading
            try {
                repository.getUserById(userId).collect { user ->
                    _currentUser.value = user?.let { RequestState.Success(it) }
                        ?: RequestState.Error(Exception("User not found"))
                    stopLoading()
                }
            } catch (e: Exception) {
                _currentUser.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }
}