package com.example.passionDaily.signup.domain.model

data class VerificationResult(
    val verification: LoginVerification,
    val formState: LoginFormState
)