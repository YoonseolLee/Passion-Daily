package com.example.passionDaily.signup.manager

import android.util.Patterns
import com.example.passionDaily.signup.domain.model.LoginFormState
import com.example.passionDaily.signup.domain.model.LoginVerification
import com.example.passionDaily.signup.domain.model.VerificationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class VerifyManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val emailPattern = Patterns.EMAIL_ADDRESS

    suspend fun verifyLoginForm(email: String): VerificationResult =
        suspendCoroutine { continuation ->
            val formState = LoginFormState(
                email = email,
                isEmailValid = true
            )

            // 1. 이메일 형식 검증
            if (!emailPattern.matcher(email).matches()) {
                continuation.resume(
                    VerificationResult(
                        LoginVerification.Error.InvalidEmailFormat,
                        formState.copy(isEmailValid = false)
                    )
                )
                return@suspendCoroutine
            }

            // 2. Firestore에서 회원 여부 검증
            firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isExistingUser = !task.result.isEmpty

                        if (isExistingUser) {
                            // 기존 회원인 경우
                            continuation.resume(
                                VerificationResult(
                                    LoginVerification.Success.ExistingUser,
                                    formState
                                )
                            )
                        } else {
                            // 새로운 회원인 경우
                            continuation.resume(
                                VerificationResult(
                                    LoginVerification.Success.NewUser,
                                    formState
                                )
                            )
                        }
                    } else {
                        continuation.resume(
                            VerificationResult(
                                LoginVerification.Error.ServerError,
                                formState
                            )
                        )
                    }
                }
        }
}
