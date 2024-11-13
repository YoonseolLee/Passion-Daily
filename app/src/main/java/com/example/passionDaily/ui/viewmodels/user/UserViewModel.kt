package com.example.passionDaily.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.relation.UserWithFavorites
import com.example.passionDaily.data.repository.UserRepository
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableStateFlow<RequestState<UserEntity?>>(RequestState.Idle)
    val user: StateFlow<RequestState<UserEntity?>> = _user

    private val _userWithFavorites =
        MutableStateFlow<RequestState<UserWithFavorites?>>(RequestState.Idle)
    val userWithFavorites: StateFlow<RequestState<UserWithFavorites?>> = _userWithFavorites

    fun getUserById(userId: Int) {
        _user.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                _user.value = RequestState.Success(user)
            } catch (e: Exception) {
                _user.value = RequestState.Error(e)
            }
        }
    }

    fun getUserWithFavorites(userId: Int) {
        _userWithFavorites.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val userWithFavorites = userRepository.getUserWithFavorites(userId)
                _userWithFavorites.value = RequestState.Success(userWithFavorites)
            } catch (e: Exception) {
                _userWithFavorites.value = RequestState.Error(e)
            }
        }
    }
}