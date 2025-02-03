package com.example.passionDaily.toast.manager

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.passionDaily.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ToastManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    fun showGeneralErrorToast() {
        showToast(R.string.error_general)
    }

    fun showCredentialErrorToast() {
        showToast(R.string.error_credential)
    }

    fun showNetworkErrorToast() {
        showToast(R.string.error_network)
    }

    fun showFirebaseErrorToast() {
        showToast(R.string.error_firebase_firestore)
    }

    fun showRoomDatabaseErrorToast() {
        showToast(R.string.error_local_database)
    }

    fun showLoginSuccessToast() {
        showToast(R.string.login_success)
    }

    fun showLogoutSuccessToast() {
        showToast(R.string.logout_success)
    }

    fun showLoginErrorToast() {
        showToast(R.string.login_error_format)
    }

    fun showAlreadyLoggedInErrorToast() {
        showToast(R.string.error_already_logged_in)
    }

    fun showAlreadyLoggedOutErrorToast() {
        showToast(R.string.error_already_logged_out)
    }

    fun showLogInRequiredErrorToast() {
        showToast(R.string.error_login_required)
    }

    fun showWithDrawlSuccessToast() {
        showToast(R.string.success_withdrawal)
    }

    fun showReLoginForWithDrawlToast() {
        showToast(R.string.re_login_forwithdrawl)
    }

    fun showURISyntaxException() {
        showToast(R.string.error_uri_syntax)
    }

    private fun showToast(@StringRes resId: Int) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }
}