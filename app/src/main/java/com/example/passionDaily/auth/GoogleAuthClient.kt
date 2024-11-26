package com.example.passionDaily.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.passionDaily.data.remote.model.user.User
import com.example.passionDaily.data.remote.model.user.UserOauth
import com.example.passionDaily.util.LoginState
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient @Inject constructor(
    private val context: Context
) {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val credentialManager = CredentialManager.create(context)
    private val clientId =
        "CLIENT_ID_추가해야함"

    // 현재 로그인 상태 확인
    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    // 현재 로그인된 사용자 정보 반환
    fun getCurrentUser(): GoogleAuthUser? = firebaseAuth.currentUser?.let { user ->
        GoogleAuthUser(
            userId = user.uid,
            username = user.displayName,
            email = user.email,
        )
    }

    /**
     * 로그인 프로세스 시작
     * 1. 이미 로그인된 경우 현재 사용자 반환
     * 2. 기존 회원인 경우 로그인 진행
     * 3. 신규 회원인 경우 회원가입 진행
     */
    suspend fun signIn(): LoginState {
        // 이미 로그인된 경우
        if (isSignedIn()) {
            return LoginState.Success(getCurrentUser())
        }

        return try {
            // 기존 회원 로그인 시도
            val loginResult = tryLoginWithExistingAccount()

            when (loginResult) {
                // 로그인 성공
                is LoginState.Success -> loginResult

                // 기존 회원이 아닌 경우 회원가입 진행
                is LoginState.Error -> tryRegisterNewAccount()
                else -> LoginState.Error("Unexpected state: $loginResult")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            LoginState.Error("Authentication failed: ${e.message}")
        }
    }

    /**
     * 기존 회원 로그인 시도
     * setFilterByAuthorizedAccounts(true)로 기존에 인증된 계정만 표시
     */
    private suspend fun tryLoginWithExistingAccount(): LoginState {
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
                LoginState.Success(getCurrentUser())
            } else {
                LoginState.Error("Login failed")
            }
        } catch (e: GetCredentialException) {
            LoginState.Error("No existing account found")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            LoginState.Error("Login failed: ${e.message}")
        }
    }

    /**
     * 신규 회원 등록 시도
     * setFilterByAuthorizedAccounts(false)로 모든 사용 가능한 계정 표시
     *
     */
    private suspend fun tryRegisterNewAccount(): LoginState {
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
                // Firestore에 사용자 정보 저장
                createNewUserInFirestore(authenticatedUser)
                LoginState.Success(getCurrentUser())
            } else {
                LoginState.Error("Registration failed")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            LoginState.Error("Registration failed: ${e.message}")
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
     * Firestore에 신규 사용자 정보 저장
     */
    private suspend fun createNewUserInFirestore(user: FirebaseUser) {
        val userDocRef = firestore.collection("users").document(user.uid)

        val newUser = createUserFromFirebaseUser(user)

        runFirestoreTransaction(userDocRef, newUser)

        saveOAuthInfo(user)
    }

    /**
     * FirebaseUser를 기반으로 User 데이터 생성
     */
    private fun createUserFromFirebaseUser(user: FirebaseUser): User {
        return User(
            id = user.uid,
            username = user.displayName ?: "",
            nickname = generateRandomNickname(),
            email = user.email ?: "",
            role = User.UserRole.USER,
            lastLoginDate = Timestamp.now(),
            notificationEnabled = true,
            promotionEnabled = false,
            isAccountDeleted = false,
            createdDate = Timestamp.now(),
            modifiedDate = Timestamp.now()
        )
    }

    /**
     * Firestore 트랜잭션 실행
     */
    private suspend fun runFirestoreTransaction(
        documentRef: DocumentReference,
        user: User
    ) {
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            if (!snapshot.exists()) {
                transaction.set(documentRef, user)
            }
        }.await()
    }

    /**
     * OAuth 정보 저장
     */
    private suspend fun saveOAuthInfo(user: FirebaseUser) {
        val oauthDoc = getOAuthDocumentRef(user.uid)

        val oauthInfo = UserOauth(
            provider = "google",
            accessToken = "",
            refreshToken = "",
            expiresAt = Timestamp.now()
        )

        oauthDoc.set(oauthInfo, SetOptions.merge()).await()
    }

    /**
     * OAuth 문서 참조 반환
     */
    private fun getOAuthDocumentRef(userId: String): DocumentReference {
        return firestore.collection("users")
            .document(userId)
            .collection("oauth")
            .document("google")
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
     * 로그아웃
     */
    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        firebaseAuth.signOut()
    }

    private fun logError(message: String) {
        println("GoogleAuthClient: $message")
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

data class GoogleAuthUser(
    // 임시 데이터 클래스
    val userId: String,
    val username: String?,
    val email: String?,
)
