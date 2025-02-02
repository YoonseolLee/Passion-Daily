package com.example.passionDaily.login.stateholder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserProfileStateHolderImpl() : UserProfileStateHolder {
    private val _isJsonValid = MutableStateFlow(false)
    override val isJsonValid: StateFlow<Boolean> = _isJsonValid

    override fun updateIsJsonValid(isValid: Boolean) {
        _isJsonValid.value = isValid
    }

    override fun isJsonValid(): Boolean {
        return _isJsonValid.value
    }
}