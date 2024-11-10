package com.example.passionDaily.ui.viewmodels.settings

import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.entity.UserSettingsEntity
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
class UserSettingsViewModel @Inject constructor(
    private val repository: PassionDailyRepository
) : BaseViewModel() {
    private val _userSettings = MutableStateFlow<RequestState<UserSettingsEntity>>(RequestState.Idle)
    val userSettings: StateFlow<RequestState<UserSettingsEntity>> = _userSettings.asStateFlow()

    fun loadUserSettings(userId: Int) {
        viewModelScope.launch {
            startLoading()
            _userSettings.value = RequestState.Loading
            try {
                val settings = repository.getUserSettings(userId)
                _userSettings.value = settings?.let { RequestState.Success(it) }
                    ?: RequestState.Error(Exception("Settings not found"))
                stopLoading()
            } catch (e: Exception) {
                _userSettings.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }

    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            startLoading()
            _userSettings.value = RequestState.Loading
            try {
                repository.updateSettings(settings)
                _userSettings.value = RequestState.Success(settings)
                stopLoading()
            } catch (e: Exception) {
                _userSettings.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }
}