package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.NetworkOnMainThreadException
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.dao.UserDao
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.local.UserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.manager.AuthenticationManager
import com.example.passionDaily.manager.UserProfileManager
import com.example.passionDaily.util.Converters
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val userDao: UserDao,
    private val authManager: AuthenticationManager,
    private val userProfileManager: UserProfileManager,
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository,
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
                val result = authManager.getGoogleCredential()
                processSignInResult(result)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Failed to retrieve credentials.")
            } catch (e: NetworkOnMainThreadException) {
                _authState.value = AuthState.Error("Network operation attempted on the main thread.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    private suspend fun processSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val idToken = authManager.extractIdToken(credential)
                val authResult = authManager.authenticateWithFirebase(idToken)
                handleAuthResult(authResult)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error("Invalid credentials provided: ${e.message}")
            } catch (e: FirebaseAuthException) {
                _authState.value = AuthState.Error("Firebase authentication failed: ${e.message}")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An error occurred during sign-in: ${e.message}")
            }
        } else {
            _authState.value = AuthState.Error("Invalid credential type received.")
        }
    }

    private suspend fun handleAuthResult(authResult: AuthResult) {
        try {
            val firebaseUser = authResult.user
            val userId = authResult.user?.uid

            if (firebaseUser != null && userId != null) {
                val userProfileMap = userProfileManager.createInitialProfile(firebaseUser, userId)
                val userProfileJson = convertMapToJson(userProfileMap)
                _userProfileJson.value = userProfileJson

                if (remoteUserRepository.isUserRegistered(userId)) {
                    remoteUserRepository.updateLastSyncDate(userId)
                    remoteUserRepository.syncFirestoreUserToRoom(userId)
                    _authState.value = AuthState.Authenticated(userId)
                } else {
                    _authState.value = AuthState.RequiresConsent(userId, userProfileJson)
                }
            } else {
                throw IllegalStateException("Firebase user or user ID is null.")
            }
        } catch (e: NullPointerException) {
            _authState.value = AuthState.Error("Unexpected null value: ${e.message}")
        } catch (e: Exception) {
            _authState.value = AuthState.Error("An error occurred: ${e.message}")
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
            // TODO: 토스트 메시지
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
