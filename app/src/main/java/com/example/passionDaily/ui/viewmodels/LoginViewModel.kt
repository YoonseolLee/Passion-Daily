package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val credentialManager: CredentialManager,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val tag = "LoginViewModel: "

    private val firestore: FirebaseFirestore = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithGoogle(
        credentialManager: CredentialManager,
        googleOption: GetSignInWithGoogleOption,
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                Log.i(tag, "getCredential() 완료")

                processSignInResult(result)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun processSignInResult(result: GetCredentialResponse) {
        var credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            try {
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    handleFirestoreUser(userId)
                } else {
                    _authState.value = AuthState.Error("User ID is null")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Authentication failed: ${e.message}")
            }
        }
    }

    private suspend fun handleFirestoreUser(userId: String) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()

            // Firestore에 사용자 정보가 없으면 등록
            if (!userDoc.exists()) {
                firestore.collection("users").document(userId).set(
                    mapOf("userId" to userId, "createdAt" to System.currentTimeMillis())
                ).await()
            }

            // Firestore 정보 확인/등록 완료 후 인증 상태로 전환
            _authState.value = AuthState.Authenticated(userId)
            Log.i(tag, "authstate: ${_authState.value}")

        } catch (e: Exception) {
            _authState.value = AuthState.Error("Firestore 처리 실패: ${e.message}")
        }
    }
}

private fun generateNonce(): String {
    val randomBytes = ByteArray(32)
    SecureRandom().nextBytes(randomBytes)
    return randomBytes.joinToString("") { "%02x".format(it) }
}

private fun createInitialProfile(firebaseUser: FirebaseUser): Map<String, Any?> {
    return mapOf(
        "id" to firebaseUser.uid,
        "nickname" to generateRandomNickname(),
        "email" to (firebaseUser.email ?: ""),
        "role" to "USER",
        "lastLoginDate" to Timestamp.now(),
        "notificationEnabled" to true,
        "promotionEnabled" to false,
        "lastSyncDate" to Timestamp.now(),
        "isAccountDeleted" to false,
        "createdDate" to Timestamp.now(),
        "modifiedDate" to Timestamp.now(),
    )
}

/**
 * 랜덤 닉네임 생성
 */
private fun generateRandomNickname(): String {
    val adjectives = listOf(
        "배고픈", "빠른", "운동하는", "행복한", "지혜로운", "용감한", "귀여운", "차분한",
        "똑똑한", "웃음많은", "말이많은", "조용한", "꿈꾸는", "성실한", "따뜻한", "시원한",
        "활발한", "졸린", "호기심많은", "웃긴", "강한", "약한", "재빠른", "느긋한",
        "밝은", "어두운", "자유로운", "친절한", "도전적인", "사려깊은", "화려한", "신비로운",
        "활기찬", "평화로운", "고독한", "명랑한", "유쾌한", "냉정한", "사랑스러운", "근면한"
    )
    val nouns = listOf(
        "독수리", "사자", "거북이", "모기", "팬더", "호랑이", "다람쥐", "고양이",
        "강아지", "토끼", "늑대", "코끼리", "여우", "원숭이", "뱀", "하마",
        "기린", "부엉이", "돼지", "참새", "펭귄", "개구리", "말", "사슴",
        "표범", "곰", "치타", "바다사자", "돌고래", "거미", "코알라", "물고기",
        "앵무새", "타조", "까치", "고래", "두더지", "고슴도치", "나비", "오리"
    )
    return "${adjectives.random()} ${nouns.random()}"
}

///**
// * 마지막 로그인 시간 업데이트
// */
//private suspend fun updateLastLoginDate(userId: String) {
//    firestore.collection("users")
//        .document(userId)
//        .update("lastLoginDate", FieldValue.serverTimestamp())
//        .await()
//}
//
//private suspend fun saveOAuthInfo(userId: String) {
//    val oauthDoc = firestore.collection("users")
//        .document(userId)
//        .collection("oauth")
//        .document("google")
//
//    val oauthInfo = UserOauth(
//        provider = "google",
//        accessToken = "",
//        refreshToken = "",
//        expiresAt = Timestamp.now()
//    )
//
//    oauthDoc.set(oauthInfo, SetOptions.merge()).await()
//}
//
///**
// * 로그아웃 - 아마 추후에 삭제 예정
// */
//suspend fun signOut() {
//    credentialManager.clearCredentialState(ClearCredentialStateRequest())
//    firebaseAuth.signOut()
//    _authState.value = AuthState.SignedOut
//}

private fun logError(message: String) {
    println("GoogleAuthClient: $message")
}
