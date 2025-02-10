package com.example.passionDaily.login.base

import android.content.Context

interface SharedLogInActions {
    fun signInWithGoogle()
    fun toggleAgreeAll()
    fun toggleIndividualItem(item: String)
    fun handleNextClick(userProfileJson: String?)
    fun openUrl(context: Context, url: String)
    fun signalLoginSuccess()
    fun handleException(e: Exception)
}
