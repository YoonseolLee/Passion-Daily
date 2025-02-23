package com.example.passionDaily.toast.manager

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.passionDaily.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ToastManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ToastManager {

    override fun showGeneralErrorToast() {
        showToast(R.string.error_general)
    }

    override fun showCredentialErrorToast() {
        showToast(R.string.error_credential)
    }

    override fun showNetworkErrorToast() {
        showToast(R.string.error_network)
    }

    override fun showFirebaseErrorToast() {
        showToast(R.string.error_firebase_firestore)
    }

    override fun showRoomDatabaseErrorToast() {
        showToast(R.string.error_local_database)
    }

    override fun showLoginSuccessToast() {
        showToast(R.string.login_success)
    }

    override fun showLogoutSuccessToast() {
        showToast(R.string.logout_success)
    }

    override fun showLoginErrorToast() {
        showToast(R.string.login_error_format)
    }

    override fun showAlreadyLoggedInErrorToast() {
        showToast(R.string.error_already_logged_in)
    }

    override fun showAlreadyLoggedOutErrorToast() {
        showToast(R.string.error_already_logged_out)
    }

    override fun showLogInRequiredErrorToast() {
        showToast(R.string.error_login_required)
    }

    override fun showWithDrawlSuccessToast() {
        showToast(R.string.success_withdrawal)
    }

    override fun showReLoginForWithDrawlToast() {
        showToast(R.string.re_login_forwithdrawl)
    }

    override fun showURISyntaxException() {
        showToast(R.string.error_uri_syntax)
    }

    override fun showInvalidEmailFormatToast() {
        showToast(R.string.error_invalid_email_format)
    }

    override fun showEmailAlreadyExistsToast() {
        showToast(R.string.error_email_already_exists)
    }

    override fun showInvalidPasswordFormatToast() {
        showToast(R.string.error_invalid_password_format)
    }

    override fun showPasswordMismatchToast() {
        showToast(R.string.error_password_mismatch)
    }

    override fun showSignupFailedToast() {
        showToast(R.string.error_signup_fail)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(@StringRes resId: Int) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }
}