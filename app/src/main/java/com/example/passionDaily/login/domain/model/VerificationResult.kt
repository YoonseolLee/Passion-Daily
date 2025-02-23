package com.example.passionDaily.login.domain.model

data class VerificationResult(
    val verification: LoginVerification,
    val formState: LoginFormState
)