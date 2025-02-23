package com.example.passionDaily.toast.manager

interface ToastManager {
    fun showGeneralErrorToast()
    fun showCredentialErrorToast()
    fun showNetworkErrorToast()
    fun showFirebaseErrorToast()
    fun showRoomDatabaseErrorToast()
    fun showLoginSuccessToast()
    fun showLogoutSuccessToast()
    fun showLoginErrorToast()
    fun showAlreadyLoggedInErrorToast()
    fun showAlreadyLoggedOutErrorToast()
    fun showLogInRequiredErrorToast()
    fun showWithDrawlSuccessToast()
    fun showReLoginForWithDrawlToast()
    fun showURISyntaxException()
    fun showInvalidEmailFormatToast()
    fun showEmailAlreadyExistsToast()
    fun showInvalidPasswordFormatToast()
    fun showPasswordMismatchToast()
    fun showSignupFailedToast()
}