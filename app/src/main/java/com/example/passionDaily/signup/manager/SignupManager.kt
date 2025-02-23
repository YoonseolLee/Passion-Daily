package com.example.passionDaily.signup.manager

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.actionCodeSettings
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SignupManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val context: Context
) {
    companion object {
        private const val EMAIL_KEY = "saved_email"
        private const val TAG = "SignupManager"
    }

    private val actionCodeSettings: ActionCodeSettings = actionCodeSettings {
        url = "https://passiondaily.page.link/email-link-login"
        handleCodeInApp = true
        setAndroidPackageName(
            "com.example.passionDaily",
            true,
            null
        )
        dynamicLinkDomain = "passiondaily.page.link"
    }

    /**
     * 이메일로 인증 링크를 전송합니다.
     */
    suspend fun sendSignInLinkToEmail(email: String): Result<Unit> =
        suspendCoroutine { continuation ->
            try {
                auth.sendSignInLinkToEmail(email, actionCodeSettings)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveEmail(email)
                            Log.d(TAG, "Email link sent successfully")
                            continuation.resume(Result.success(Unit))
                        } else {
                            val exception = task.exception ?: Exception("Unknown error")
                            Log.e(TAG, "Error sending email link", exception)
                            continuation.resume(Result.failure(exception))
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendSignInLinkToEmail", e)
                continuation.resume(Result.failure(e))
            }
        }

    /**
     * 이메일 링크로 로그인을 완료합니다.
     */
    suspend fun completeSignIn(email: String, emailLink: String): Result<FirebaseUser> =
        suspendCoroutine { continuation ->
            try {
                auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                continuation.resume(Result.success(user))
                            } else {
                                continuation.resume(Result.failure(Exception("User is null")))
                            }
                        } else {
                            val exception = task.exception ?: Exception("Unknown error")
                            continuation.resume(Result.failure(exception))
                        }
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }

    private fun saveEmail(email: String) {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(EMAIL_KEY, email)
            .apply()
    }

    fun getSavedEmail(): String? {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString(EMAIL_KEY, null)
    }
}