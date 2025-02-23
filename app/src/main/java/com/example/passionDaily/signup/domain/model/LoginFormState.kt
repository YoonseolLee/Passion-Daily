package com.example.passionDaily.signup.domain.model

data class LoginFormState(
    val email: String = "",
    val isEmailValid: Boolean = false,
)