package com.example.passionDaily.navigation

sealed class NavAction {
    object NavigateToQuoteScreen : NavAction()
    object NavigateToLoginScreen : NavAction()
    object NavigateToSignUpScreen : NavAction()
}