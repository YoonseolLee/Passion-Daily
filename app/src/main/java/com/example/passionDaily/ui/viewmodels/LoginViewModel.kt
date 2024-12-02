package com.example.passionDaily.ui.viewmodels

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.remote.dto.UserDTO
import com.example.passionDaily.data.remote.model.user.User
import com.example.passionDaily.data.remote.model.user.UserOauth
import com.example.passionDaily.util.LoginState
import com.example.passionDaily.util.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val context: Context,
    private val clientId: String
) : ViewModel() {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val credentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _pendingUserMap = MutableStateFlow<Map<String, Any>?>(null)
    val pendingUserMap: StateFlow<Map<String, Any>?> = _pendingUserMap.asStateFlow()

    init {
        checkInitialAuthState()
    }

    private fun checkInitialAuthState() {
        viewModelScope.launch {
            _authState.value = when {
                // 로그인되어있는 상태
                isSignedIn() -> {
                    val currentUser = getCurrentUser()
                    if (currentUser != null) {
                        AuthState.SignedIn(currentUser)
                    } else {
                        AuthState.SignedOut
                    }
                }

                else -> AuthState.SignedOut
            }
        }
    }

    // 현재 로그인 상태 확인
    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    // 현재 로그인된 사용자 정보 반환
    suspend fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null

        return try {
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                userDoc.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logError("Error fetching current user: ${e.message}")
            null
        }
    }

    fun onGoogleLoginClicked() {
        viewModelScope.launch {
            val state = signIn()
            _authState.value = state
        }
    }

    /**
     * 로그인 프로세스
     * 2. 최근 로그인 이력이 없는 경우: SignedOut
     * 2-1. 로그인 시도한 구글계정이 회원인지 검증 - 회원인 경우: SignedOut
     * -> 해당 아이디로 앱 접속 및 메인화면인 QuoteScreen 바로 렌더링: SignedIn
     *
     * 2-2. 로그인 시도한 구글계정이 회원인지 검증 - 비회원인 경우: SignUpRequired
     * -> 해당 아이디로 회원가입 처리 및 파이어스토어에 회원 정보 저장 후, QuoteScreen 이동: SignedIn
     */
    suspend fun signIn(): AuthState {

        return try {
            // 회원인지 검증
            val loginResult = tryLoginWithExistingAccount()

            when (loginResult) {
                is AuthState.SignedIn -> {
                    _authState.value = loginResult
                    loginResult
                }

                is AuthState.SignUpRequired -> {
                    _authState.value = AuthState.SignUpRequired
                    val pendingUserMap = initiateRegisterNewAccount()
                    AuthState.SignUpRequired
                }

                else -> {
                    val errorState = AuthState.Error("Unexpected state")
                    _authState.value = errorState
                    errorState
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val errorState = AuthState.Error("Authentication failed: ${e.message}")
            _authState.value = errorState
            errorState
        }
    }

    /**
     * 기존 회원 로그인 시도
     * setFilterByAuthorizedAccounts(true)로 기존에 인증된 계정만 표시
     */
    private suspend fun tryLoginWithExistingAccount(): AuthState {
        return try {
            val credential = requestGoogleCredential(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(true)
                    .build()
            )

            val authenticatedUser = authenticateWithFirebase(credential)

            if (authenticatedUser != null) {
                updateLastLoginDate(authenticatedUser.uid)
                val currentUser = getCurrentUser()

                if (currentUser != null) {
                    AuthState.SignedIn(currentUser)
                } else {
                    AuthState.SignedOut
                }
            } else {
                AuthState.Error("Login failed")
            }
        } catch (e: GetCredentialException) {
            AuthState.Error("No existing account found")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthState.Error("Login failed: ${e.message}")
        }
    }

    /**
     * 신규 회원 등록 시도
     * setFilterByAuthorizedAccounts(false)로 모든 사용 가능한 계정 표시
     *
     */
    private suspend fun initiateRegisterNewAccount(): AuthState {
        return try {
            val credential = requestGoogleCredential(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()
            )

            val authenticatedUser = authenticateWithFirebase(credential)

            if (authenticatedUser != null) {
                val pendingUserMap = createInitialProfile(authenticatedUser)

                // SelectGenderAndAgeGroupScreen으로 이동할 수 있도록 SignUpRequired 상태 반환
                AuthState.SignUpRequired
            } else {
                AuthState.Error("Registration failed")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthState.Error("Registration failed: ${e.message}")
        }
    }

    /**
     * Google Credential 요청
     */
    private suspend fun requestGoogleCredential(
        googleIdOption: GetGoogleIdOption
    ): CustomCredential {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = credentialManager.getCredential(context, request)
        val credential = response.credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Invalid credential type received")
        }

        return credential
    }

    /**
     * Firebase 인증 처리
     */
    private suspend fun authenticateWithFirebase(
        credential: CustomCredential
    ): FirebaseUser? {
        return try {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).await()
            authResult.user
        } catch (e: GoogleIdTokenParsingException) {
            logError("Failed to parse Google ID token: ${e.message}")
            null
        }
    }

    /**
     * FirebaseUser를 기반으로 UserDTOpendingUserMap 생성 (gender, ageGroup은 null)
     * 이후 SelectGenderAndAgeGroupScreen으로 전달
     */
    private fun createInitialProfile(firebaseUser: FirebaseUser): Map<String, Any?> {

        return mapOf(
            "id" to firebaseUser.uid,
            "nickname" to generateRandomNickname(),
            "email" to (firebaseUser.email ?: ""),
            "role" to "USER",
            "gender" to null,
            "ageGroup" to null,
            "lastLoginDate" to Timestamp.now(),
            "notificationEnabled" to true,
            "promotionEnabled" to false,
            "lastSyncDate" to Timestamp.now(),
            "isAccountDeleted" to false,
            "createdDate" to Timestamp.now(),
            "modifiedDate" to Timestamp.now()
        )
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    private suspend fun updateLastLoginDate(userId: String) {
        firestore.collection("users")
            .document(userId)
            .update("lastLoginDate", FieldValue.serverTimestamp())
            .await()
    }

    /**
     * 로그아웃 - 아마 추후에 삭제 예정
     */
    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        firebaseAuth.signOut()
        _authState.value = AuthState.SignedOut
    }

    private fun logError(message: String) {
        println("GoogleAuthClient: $message")
    }

    private suspend fun saveOAuthInfo(userId: String) {
        val oauthDoc = firestore.collection("users")
            .document(userId)
            .collection("oauth")
            .document("google")

        val oauthInfo = UserOauth(
            provider = "google",
            accessToken = "",
            refreshToken = "",
            expiresAt = Timestamp.now()
        )

        oauthDoc.set(oauthInfo, SetOptions.merge()).await()
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
}
