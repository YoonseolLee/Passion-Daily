package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.passionDaily.data.local.dao.UserDao
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.remote.model.User
import com.example.passionDaily.data.repository.local.UserRepository
import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Converters
import com.example.passionDaily.util.Gender
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


@HiltViewModel
class SharedSignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val tag = "SharedSignInViewModel: "

    private val firestore: FirebaseFirestore = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userProfileJson = MutableStateFlow<String?>(null)
    val userProfileJson: StateFlow<String?> = _userProfileJson.asStateFlow()

    private val _userProfileJsonV2 = MutableStateFlow<String?>(null)
    val userProfileJsonV2: StateFlow<String?> = _userProfileJsonV2.asStateFlow()

    private val _isJsonValid = MutableLiveData<Boolean>()
    val isJsonValid: LiveData<Boolean> get() = _isJsonValid

    private val _isAgreeAllChecked = MutableStateFlow(false)
    val isAgreeAllChecked: StateFlow<Boolean> = _isAgreeAllChecked.asStateFlow()

    private val _termsOfServiceChecked = MutableStateFlow(false)
    val termsOfServiceChecked: StateFlow<Boolean> = _termsOfServiceChecked.asStateFlow()

    private val _privacyPolicyChecked = MutableStateFlow(false)
    val privacyPolicyChecked: StateFlow<Boolean> = _privacyPolicyChecked.asStateFlow()

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
                        // Firestore에 정보 업데이트
                        updateLastSyncDateOnFirestore(userId)

                        // room db에 동기화
                        syncFirestoreUserToRoom(userId, firestore)

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

    private suspend fun updateLastSyncDateOnFirestore(userId: String) {
        try {
            val now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            firestore.collection("users")
                .document(userId)
                .update(
                    "lastSyncDate", now,
                    "lastLoginDate", now
                )
                .await()

            Log.d(
                "UserSync",
                "lastSyncDate와 lastLoginDate 업데이트 성공: lastSyncDate = $now, lastLoginDate = $now"
            )
        } catch (e: Exception) {
            Log.e("UserSync", "Firestore에서 lastSyncDate 또는 lastLoginDate 업데이트 실패: ${e.message}")
        }
    }

    suspend fun syncFirestoreUserToRoom(
        userId: String,
        firestoreDb: FirebaseFirestore,
    ) {
        try {
            // Firestore에서 사용자 데이터 가져오기
            val userDoc = firestoreDb
                .collection("users")
                .document(userId)
                .get()
                .await()

            val firestoreUser = userDoc.toObject(User::class.java)
                ?: throw Exception("Firestore에서 사용자 데이터를 찾을 수 없습니다")

            // Firestore User 객체를 Room UserEntity로 변환
            val userEntity = UserEntity(
                userId = firestoreUser.id,
                email = firestoreUser.email,
                notificationEnabled = firestoreUser.notificationEnabled,
                notificationTime = firestoreUser.notificationTime,
                lastSyncDate = parseTimestamp(firestoreUser.lastSyncDate),
                isAccountDeleted = firestoreUser.isAccountDeleted,
            )

            userDao.insertUser(userEntity)

        } catch (e: Exception) {
            Log.e("UserSync", "사용자 데이터 동기화 실패: ${e.message}")
            throw e
        }
    }

    fun parseTimestamp(timestamp: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = dateFormat.parse(timestamp)
                ?: throw IllegalArgumentException("유효하지 않은 날짜 형식: $timestamp")

            Log.d("parseTimestamp", "date.time: ${date.time}")
            date.time
        } catch (e: Exception) {
            Log.e("TimestampParsing", "Timestamp 파싱 실패: $timestamp", e)
            System.currentTimeMillis()
        }
    }

    private fun createInitialProfile(
        firebaseUser: FirebaseUser,
        userId: String
    ): Map<String, Any?> {
        val now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        Log.d(tag, "now: ${now}")

        return mapOf(
            "id" to userId,
            "email" to (firebaseUser.email),
            "role" to "USER",
            "lastLoginDate" to now,
            "notificationEnabled" to true,
            "notificationTime" to "08:00",
            "privacyPolicyEnabled" to null,
            "termsOfServiceEnabled" to null,
            "lastSyncDate" to now,
            "isAccountDeleted" to false,
            "createdDate" to now,
            "modifiedDate" to now,
        )
    }

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

    // 전체 동의 토글 메서드
    fun toggleAgreeAll() {
        val currentState = !_isAgreeAllChecked.value
        _isAgreeAllChecked.value = currentState
        _termsOfServiceChecked.value = currentState
        _privacyPolicyChecked.value = currentState
    }

    // 개별 항목 토글 메서드
    fun toggleIndividualItem(item: String) {
        when (item) {
            "termsOfService" -> {
                _termsOfServiceChecked.value = !_termsOfServiceChecked.value
                updateAgreeAllState()
            }

            "privacyPolicy" -> {
                _privacyPolicyChecked.value = !_privacyPolicyChecked.value
                updateAgreeAllState()
            }
        }
    }

    // 개별 체크박스 상태에 따라 전체 동의 상태 업데이트
    private fun updateAgreeAllState() {
        _isAgreeAllChecked.value = _termsOfServiceChecked.value && _privacyPolicyChecked.value
    }

    // 다음 버튼 클릭 핸들러
    fun handleNextClick(userProfileJson: String?) {
        if (_termsOfServiceChecked.value && _privacyPolicyChecked.value) {
            Log.d(tag, "Terms of Service Checked: ${_termsOfServiceChecked.value}")
            Log.d(tag, "Privacy Policy Checked: ${_privacyPolicyChecked.value}")

            val userProfileJsonV2 = handleUserProfileJson(userProfileJson)
            _userProfileJsonV2.value = userProfileJsonV2

            userProfileJsonV2?.let { userProfileJsonV2 ->
                viewModelScope.launch {
                    // Firebase에 회원정보를 저장한다.
                    addUserProfileToFireStore(userProfileJsonV2)

                    // Local DB에 저장한다.
                    addUserProfileToRoomDB(userProfileJsonV2)
                }
            }
        } else {
            Log.e(tag, "User did not agree to required terms")
        }
    }

    // JSON 처리 로직에서 마케팅 수신 관련 항목 제거
    fun handleUserProfileJson(userProfileJson: String?): String? {
        if (_isJsonValid.value == true) {
            try {
                val userProfileJsonObject = JSONObject(userProfileJson)

                val consentStatesMap = mapOf(
                    "termsOfServiceChecked" to _termsOfServiceChecked.value,
                    "privacyPolicyChecked" to _privacyPolicyChecked.value
                )
                Log.d("SharedSignInViewModel", "updatedConsentStates: $consentStatesMap")

                userProfileJsonObject.put(
                    "privacyPolicyEnabled",
                    consentStatesMap["privacyPolicyChecked"] ?: false
                )
                userProfileJsonObject.put(
                    "termsOfServiceEnabled",
                    consentStatesMap["termsOfServiceChecked"] ?: false
                )

                Log.i(
                    "SharedSignInViewModel",
                    "Updated userProfileJsonObject: $userProfileJsonObject"
                )

                return userProfileJsonObject.toString()
            } catch (e: JSONException) {
                Log.e("SharedSignInViewModel", "JSON 처리 중 오류 발생", e)
            }
        }
        return null
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private suspend fun addUserProfileToFireStore(userProfileJsonV2: String) {
        try {
            val gson = Gson()

            val userProfileMap: Map<String, Any?> = gson.fromJson(
                userProfileJsonV2,
                object : TypeToken<Map<String, Any?>>() {}.type
            )

            val id = userProfileMap["id"] as String

            Log.d("addUserProfileToFireStore", "userProfileMap: ${userProfileMap}")

            val userDoc = firestore.collection("users").document(id).get().await()

            // Firestore에 사용자 정보가 없으면 등록
            if (!userDoc.exists()) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    firestore.collection("users").document(id).set(userProfileMap).await()
                }
            }

            // Firestore 정보 확인/등록 완료 후 인증 상태로 전환
            _authState.value = AuthState.Authenticated(id)
            Log.i(tag, "authstate: ${_authState.value}")
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Firestore 처리 실패: ${e.message}")
        }
    }

    private suspend fun addUserProfileToRoomDB(userProfileJsonV2: String) {
        try {
            val gson = Gson()

            val userProfileMap: Map<String, Any?> = gson.fromJson(
                userProfileJsonV2,
                object : TypeToken<Map<String, Any?>>() {}.type
            )

            // JSON 데이터를 UserEntity로 매핑
            val userEntity = UserEntity(
                userId = userProfileMap["id"] as String,
                email = userProfileMap["email"] as String,
                notificationEnabled = userProfileMap["notificationEnabled"] as Boolean,
                lastSyncDate = (userProfileMap["lastSyncDate"] as String).let {
                    Converters.fromStringToLong(it)
                },
                notificationTime = userProfileMap["notificationTime"] as String,
                isAccountDeleted = userProfileMap["isAccountDeleted"] as Boolean,
            )

            // Room DB에 저장
            userRepository.insertUser(userEntity)
            Log.i(tag, "User Profile added to Room DB: $userEntity")
        } catch (e: Exception) {
            Log.e(tag, "Failed to add user profile to Room DB: ${e.message}", e)
        }
    }

    private fun convertMapToJson(map: Map<String, Any?>): String {
        val gson = GsonBuilder()
            .serializeNulls()
            .create()
        return gson.toJson(map)
    }
}
