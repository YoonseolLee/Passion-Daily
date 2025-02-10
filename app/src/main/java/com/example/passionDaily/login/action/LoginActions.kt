package com.example.passionDaily.login.action

import com.example.passionDaily.login.base.SharedLogInActions

interface LoginActions : SharedLogInActions {
    override fun signInWithGoogle()
    override fun signalLoginSuccess()
    override fun handleException(e: Exception)
}