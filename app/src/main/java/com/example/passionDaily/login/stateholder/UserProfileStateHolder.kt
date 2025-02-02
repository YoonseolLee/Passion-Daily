package com.example.passionDaily.login.stateholder

import kotlinx.coroutines.flow.StateFlow

interface UserProfileStateHolder {
    val isJsonValid: StateFlow<Boolean>

    fun updateIsJsonValid(isValid: Boolean)

    fun isJsonValid(): Boolean
}