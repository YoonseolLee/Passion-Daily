package com.example.passionDaily.login.domain.model

data class LoginFormState(
    val email: String = "",
    val isEmailValid: Boolean = false,
)