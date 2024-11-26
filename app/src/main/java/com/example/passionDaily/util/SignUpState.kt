package com.example.passionDaily.util

import com.example.passionDaily.auth.GoogleAuthUser

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val user: GoogleAuthUser?) : SignUpState()
    data class Error(val message: String) : SignUpState()
}