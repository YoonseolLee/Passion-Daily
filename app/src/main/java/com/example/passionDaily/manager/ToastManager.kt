package com.example.passionDaily.manager

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

    fun showNetworkErrorToast() {
        showToast(R.string.error_network_retry)
    }

    fun showFirebaseErrorToast() {
        showToast(R.string.error_firebase_firestore)
    }

    fun showLoginSuccessMessage() {
        showToast(R.string.login_success)
    }

    fun showLoginErrorMessage() {
        showToast(R.string.login_error_format)
    }

    private fun showToast(@StringRes resId: Int) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }
}