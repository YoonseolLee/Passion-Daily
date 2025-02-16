package com.example.passionDaily.login.presentation.viewmodel

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.login.state.AuthState.Unauthenticated
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.util.MainCoroutineRule
import com.example.passionDaily.util.mapper.UserProfileMapper
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class SharedLogInViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val authManager = mockk<AuthenticationManager>()
    private val userProfileManager = mockk<UserProfileManager>()
    private val userConsentManager = mockk<UserConsentManager>()
    private val remoteUserRepository = mockk<RemoteUserRepository>()
    private val userProfileMapper = mockk<UserProfileMapper>()
    private val authStateHolder = mockk<AuthStateHolder>()
    private val loginStateHolder = mockk<LoginStateHolder>()
    private val toastManager = mockk<ToastManager>()
    private val stringProvider = mockk<StringProvider>()
    private val urlManager = mockk<UrlManager>()

    private lateinit var viewModel: SharedLogInViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(Log::class)
        mockkStatic(TextUtils::class)
        mockkStatic(android.util.SparseArray::class)

        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { TextUtils.isEmpty(any()) } returns false

        every { loginStateHolder.isLoading } returns MutableStateFlow(false)
        every { loginStateHolder.userProfileJson } returns MutableStateFlow("")
        every { loginStateHolder.userProfileJsonV2 } returns MutableStateFlow("")
        every { authStateHolder.authState } returns MutableStateFlow(Unauthenticated)
        every { userConsentManager.consent } returns MutableStateFlow(
            UserConsent(
                termsOfService = false,
                privacyPolicy = false
            )
        )
        every { userConsentManager.isAgreeAllChecked } returns MutableStateFlow(false)
        every { stringProvider.getString(any()) } returns "Error message"

        // ToastManager 함수들 모킹
        coEvery { toastManager.showNetworkErrorToast() } just Runs
        coEvery { toastManager.showCredentialErrorToast() } just Runs
        coEvery { toastManager.showGeneralErrorToast() } just Runs
        coEvery { toastManager.showFirebaseErrorToast() } just Runs

        viewModel = SharedLogInViewModel(
            authManager = authManager,
            userProfileManager = userProfileManager,
            userConsentManager = userConsentManager,
            urlManager = urlManager,
            remoteUserRepository = remoteUserRepository,
            toastManager = toastManager,
            userProfileMapper = userProfileMapper,
            stringProvider = stringProvider,
            authStateHolder = authStateHolder,
            loginStateHolder = loginStateHolder
        )
    }

    @Test
    fun `구글 로그인시 기존 유저면 유저 데이터를 동기화한다`() = mainCoroutineRule.runTest {
        // given
        val userId = "test_user_id"
        val userProfileJson = "{}"
        val credential = mockk<CustomCredential> {
            every { type } returns GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        }
        val response = mockk<GetCredentialResponse> {
            every { this@mockk.credential } returns credential
        }
        val authResult = mockk<AuthResult>()
        val firebaseUser = mockk<FirebaseUser>()

        coEvery { authManager.startLoading() } just Runs
        coEvery { authManager.stopLoading() } just Runs
        coEvery { authManager.clearCredentials() } just Runs
        coEvery { authManager.getGoogleCredential() } returns response
        coEvery { authManager.extractIdToken(any()) } returns "test_token"
        coEvery { authManager.authenticateWithFirebase(any()) } returns authResult
        coEvery { authManager.getFirebaseUser(any()) } returns firebaseUser
        coEvery { authManager.getUserId(any()) } returns userId
        coEvery {
            userProfileManager.createInitialProfile(
                any(),
                any()
            )
        } returns mapOf("key" to "value")
        coEvery { userProfileMapper.convertMapToJson(any()) } returns userProfileJson
        coEvery { authManager.updateUserProfileJson(any()) } just Runs
        coEvery { remoteUserRepository.isUserRegistered(userId) } returns true
        coEvery { userProfileManager.syncExistingUser(userId) } just Runs

        // when
        viewModel.signInWithGoogle()

        // then
        coVerify {
            authManager.startLoading()
            authManager.clearCredentials()
            authManager.getGoogleCredential()
            authManager.extractIdToken(any())
            authManager.authenticateWithFirebase(any())
            userProfileManager.syncExistingUser(userId)
            authManager.stopLoading()
        }
        coVerify(exactly = 0) { authStateHolder.setRequiresConsent(any(), any()) }
    }

    @Test
    fun `구글 로그인시 신규 유저면 컨센트가 필요하다`() = mainCoroutineRule.runTest {
        // given
        val userId = "test_user_id"
        val userProfileJson = "{}"
        val credential = mockk<CustomCredential> {
            every { type } returns GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        }
        val response = mockk<GetCredentialResponse> {
            every { this@mockk.credential } returns credential
        }
        val authResult = mockk<AuthResult>()
        val firebaseUser = mockk<FirebaseUser>()

        coEvery { authManager.startLoading() } just Runs
        coEvery { authManager.stopLoading() } just Runs
        coEvery { authManager.clearCredentials() } just Runs
        coEvery { authManager.getGoogleCredential() } returns response
        coEvery { authManager.extractIdToken(any()) } returns "test_token"
        coEvery { authManager.authenticateWithFirebase(any()) } returns authResult
        coEvery { authManager.getFirebaseUser(any()) } returns firebaseUser
        coEvery { authManager.getUserId(any()) } returns userId
        coEvery {
            userProfileManager.createInitialProfile(
                any(),
                any()
            )
        } returns mapOf("key" to "value")
        coEvery { userProfileMapper.convertMapToJson(any()) } returns userProfileJson
        coEvery { authManager.updateUserProfileJson(any()) } just Runs
        coEvery { remoteUserRepository.isUserRegistered(userId) } returns false
        coEvery { authStateHolder.setRequiresConsent(any(), any()) } just Runs

        // when
        viewModel.signInWithGoogle()

        // then
        coVerify {
            authManager.startLoading()
            authManager.clearCredentials()
            authStateHolder.setRequiresConsent(userId, userProfileJson)
            authManager.stopLoading()
        }
        coVerify(exactly = 0) { userProfileManager.syncExistingUser(any()) }
    }

    @Test
    fun `verifyUserProfileJson은 json이 null이 아닐 때 검증을 수행한다`() = mainCoroutineRule.runTest {
        // given
        val json = "{\"test\": \"value\"}"
        coEvery { userProfileManager.verifyJson(json) } returns true

        // when
        viewModel.verifyUserProfileJson(json)

        // then
        coVerify { userProfileManager.verifyJson(json) }
    }

    @Test
    fun `verifyUserProfileJson은 json이 null일 때 verifyJson을 호출한다`() = mainCoroutineRule.runTest {
        // given
        coEvery { userProfileManager.verifyJson(null) } returns false

        // when
        viewModel.verifyUserProfileJson(null)

        // then
        coVerify(exactly = 1) { userProfileManager.verifyJson(null) }
    }

    @Test
    fun `toggleIndividualItem은 UserConsentManager의 toggleIndividualItem을 호출한다`() {
        // given
        val item = "termsOfService"
        every { userConsentManager.toggleIndividualItem(any()) } just runs

        // when
        viewModel.toggleIndividualItem(item)

        // then
        verify { userConsentManager.toggleIndividualItem(item) }
    }

    @Test
    fun `handleNextClick은 모든 약관에 동의하지 않았을 때 프로필을 저장하지 않는다`() = mainCoroutineRule.runTest {
        // given
        val userProfileJson = "test_json"
        every { userConsentManager.consent.value } returns UserConsent(
            termsOfService = false,
            privacyPolicy = false
        )

        // when
        viewModel.handleNextClick(userProfileJson)

        // then
        coVerify(exactly = 0) { userProfileManager.updateUserProfileWithConsent(any(), any()) }
    }

    @Test
    fun `openUrl은 UrlManager의 openUrl을 호출한다`() = mainCoroutineRule.runTest {
        // given
        val context = mockk<Context>()
        val url = "https://example.com"
        every { urlManager.openUrl(any(), any()) } just runs

        // when
        viewModel.openUrl(context, url)

        // then
        verify { urlManager.openUrl(context, url) }
    }

    @Test
    fun `signalLoginSuccess는 updateIsLoggedIn과 showLoginSuccessToast를 호출한다`() =
        mainCoroutineRule.runTest {
            // Given
            coEvery { authManager.updateIsLoggedIn(true) } just Runs
            coEvery { toastManager.showLoginSuccessToast() } just Runs

            // When
            viewModel.signalLoginSuccess()
            advanceTimeBy(150)

            // Then
            coVerify(exactly = 1) { authManager.updateIsLoggedIn(true) }
            coVerify(exactly = 1) { toastManager.showLoginSuccessToast() }
        }
}