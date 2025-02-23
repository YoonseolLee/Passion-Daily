package com.example.passionDaily.signup.domain.model

sealed class LoginVerification {
    sealed class Success : LoginVerification() {
        object ExistingUser : Success()
        object NewUser : Success()
    }
    sealed class Error : LoginVerification() {
        object InvalidEmailFormat : Error()
        object ServerError : Error()
    }
}