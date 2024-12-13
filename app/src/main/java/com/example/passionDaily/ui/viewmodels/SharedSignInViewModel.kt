package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


@HiltViewModel
class SharedSignInViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val tag = "LoginViewModel: "

    private val firestore: FirebaseFirestore = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userProfileJson = MutableStateFlow<String?>(null)
    val userProfileJson: StateFlow<String?> = _userProfileJson.asStateFlow()

    private val _isJsonValid = MutableLiveData<Boolean>()
    val isJsonValid: LiveData<Boolean> get() = _isJsonValid

    /**
     * LoginScreen
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val credentialManager = CredentialManager.create(context)
                val clientId = context.getString(R.string.client_id)

                val googleIdOption =
                    GetSignInWithGoogleOption.Builder(clientId)
                        .build()
                Log.i(tag, "googleIdOption: ${googleIdOption}")

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                Log.i(tag, "request: ${request}")

                val result = credentialManager.getCredential(context, request)
                Log.i(tag, "getCredential(): ${result}")

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
                val firebaseUser = auth.currentUser
                val userId = authResult.user?.uid

                if (firebaseUser != null && userId != null) {
                    val userProfileMap = createInitialProfile(firebaseUser, userId)
                    val userProfileJson = convertMapToJson(userProfileMap)

                    // Firestore에 회원 정보가 있는지 확인
                    val isRegistered = isUserRegisteredInFirestore(userId)

                    _userProfileJson.value = userProfileJson

                    if (isRegistered) {
                        // 회원일 경우 바로 QuoteScreen으로 이동
                        _authState.value = AuthState.Authenticated(userId)
                    } else {
                        // 비회원일 경우 TermsConsentScreen으로 이동
                        _authState.value = AuthState.RequiresConsent(userId, userProfileJson)
                    }
                } else {
                    _authState.value = AuthState.Error("User information is not available.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Authentication failed: ${e.message}")
            }
        }
    }

    //                if (userId != null) {
//                    saveInitialProfileInFirestore(userId)
//                    // TODO: saveInitialProfileInRoomDB 작성
//                    // saveInitialProfileInRoomDB
//                } else {
//                    _authState.value = AuthState.Error("User ID is null")
//                }

    private suspend fun isUserRegisteredInFirestore(userId: String): Boolean {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            documentSnapshot.exists()
        } catch (e: Exception) {
            Log.e(tag, "Error checking user registration: ${e.message}")
            false
        }
    }


    /**
     * TermsConsentScreen
     */
    fun verifyUserProfileJson(json: String?) {
        if (json.isNullOrEmpty()) {
            Log.e("SharedSignInViewModel", "Invalid JSON: null or empty")
            _isJsonValid.value = false
        } else {
            try {
                JSONObject(json)
                Log.d("SharedSignInViewModel", "Valid JSON: $json")
                _isJsonValid.value = true
            } catch (e: JSONException) {
                Log.e("SharedSignInViewModel", "Invalid JSON format: $json", e)
                _isJsonValid.value = false
            }
        }
    }

    /**
     * SelectGenderAndAgeGroupScreen
     */


    private suspend fun saveInitialProfileInFirestore(userId: String) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()

            // Firestore에 사용자 정보가 없으면 등록
            if (!userDoc.exists()) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val initialProfile = createInitialProfile(firebaseUser, userId)
                    firestore.collection("users").document(userId).set(initialProfile).await()
                }
            }

            // Firestore 정보 확인/등록 완료 후 인증 상태로 전환
            _authState.value = AuthState.Authenticated(userId)
            Log.i(tag, "authstate: ${_authState.value}")

        } catch (e: Exception) {
            _authState.value = AuthState.Error("Firestore 처리 실패: ${e.message}")
        }
    }

    private fun createInitialProfile(
        firebaseUser: FirebaseUser,
        userId: String
    ): Map<String, Any?> {
        val now = Timestamp.now()

        return mapOf(
            "id" to userId,
            "nickname" to generateRandomNickname(),
            "email" to (firebaseUser.email ?: ""),
            "role" to "USER",
            "gender" to null,
            "lastLoginDate" to now,
            "notificationEnabled" to true,
            "marketingConsentEnabled" to null,
            "privacyPolicyEnabled" to null,
            "termsOfServiceEnabled" to null,
            "lastSyncDate" to null,
            "isAccountDeleted" to false,
            "createdDate" to now,
            "modifiedDate" to now,
        )
    }

    private fun convertMapToJson(map: Map<String, Any?>): String {
        val gson = GsonBuilder()
            .serializeNulls()
            .create()
        return gson.toJson(map)
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

    private fun logError(message: String) {
        println("GoogleAuthClient: $message")
    }
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

//        return mapOf(
//            "id" to userId,
//            "nickname" to generateRandomNickname(),
//            "email" to (firebaseUser.email ?: ""),
//            "role" to "USER",
//            "lastLoginDate" to now,
//            "notificationEnabled" to true,
//            "marketingConsentEnabled" to false,
//            "privacyPolicyEnabled" to false,
//            "termsOfServiceEnabled" to false,
//            "lastSyncDate" to null,
//            "isAccountDeleted" to false,
//            "createdDate" to now,
//            "modifiedDate" to now,
//        )
