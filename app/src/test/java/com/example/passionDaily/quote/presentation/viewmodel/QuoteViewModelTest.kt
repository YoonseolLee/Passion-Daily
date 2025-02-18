package com.example.passionDaily.quote.presentation.viewmodel

import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.login.presentation.viewmodel.SharedLogInViewModel
import com.example.passionDaily.login.state.AuthState.Unauthenticated
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.util.MainCoroutineRule
import com.example.passionDaily.util.mapper.UserProfileMapper
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val toastManager = mockk<ToastManager>()
    private val userProfileMapper = mockk<UserProfileMapper>()
    private val stringProvider = mockk<StringProvider>()
    private val authStateHolder = mockk<AuthStateHolder>()
    private val loginStateHolder = mockk<LoginStateHolder>()
    private val urlManager = mockk<UrlManager>()

    private lateinit var viewModel: SharedLogInViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(Log::class)

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

        coEvery { toastManager.showNetworkErrorToast() } just Runs
        coEvery { toastManager.showCredentialErrorToast() } just Runs
        coEvery { toastManager.showGeneralErrorToast() } just Runs
        coEvery { toastManager.showFirebaseErrorToast() } just Runs
        coEvery { toastManager.showLoginSuccessToast() } just Runs

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
    fun `구글_로그인_성공시_신규_사용자는_동의화면으로_이동한다`() = mainCoroutineRule.runTest {
        // given
        val userId = "testUserId"
        val credential = mockk<CustomCredential> {
            every { type } returns GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        }
        val response = mockk<GetCredentialResponse> {
            every { this@mockk.credential } returns credential
        }
        val authResult = mockk<AuthResult>()
        val firebaseUser = mockk<FirebaseUser>()
        val userProfileJson = "{}"

        setupSuccessfulLoginMocks(
            response = response,
            authResult = authResult,
            firebaseUser = firebaseUser,
            userId = userId,
            userProfileJson = userProfileJson,
            isRegistered = false
        )

        // when
        viewModel.signInWithGoogle()

        // then
        coVerify(exactly = 1) {
            authManager.startLoading()
            authManager.clearCredentials()
            authManager.getGoogleCredential()
            authManager.extractIdToken(credential)
            authManager.authenticateWithFirebase("test_token")
            authManager.getFirebaseUser(authResult)
            authManager.getUserId(firebaseUser)
            userProfileManager.createInitialProfile(firebaseUser, userId)
            userProfileMapper.convertMapToJson(any())
            authManager.updateUserProfileJson(userProfileJson)
            remoteUserRepository.isUserRegistered(userId)
            authStateHolder.setRequiresConsent(userId, userProfileJson)
            authManager.stopLoading()
        }
    }

    @Test
    fun `구글_로그인_성공시_기존_사용자는_프로필_동기화한다`() = mainCoroutineRule.runTest {
        // given
        val userId = "testUserId"
        val credential = mockk<CustomCredential> {
            every { type } returns GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        }
        val response = mockk<GetCredentialResponse> {
            every { this@mockk.credential } returns credential
        }
        val authResult = mockk<AuthResult>()
        val firebaseUser = mockk<FirebaseUser>()
        val userProfileJson = "{}"

        setupSuccessfulLoginMocks(
            response = response,
            authResult = authResult,
            firebaseUser = firebaseUser,
            userId = userId,
            userProfileJson = userProfileJson,
            isRegistered = true
        )

        // when
        viewModel.signInWithGoogle()

        // then
        coVerify { userProfileManager.syncExistingUser(userId) }
    }

    private fun setupSuccessfulLoginMocks(
        response: GetCredentialResponse,
        authResult: AuthResult,
        firebaseUser: FirebaseUser,
        userId: String,
        userProfileJson: String,
        isRegistered: Boolean
    ) {
        coEvery { authManager.startLoading() } just Runs
        coEvery { authManager.stopLoading() } just Runs
        coEvery { authManager.clearCredentials() } just Runs
        coEvery { authManager.getGoogleCredential() } returns response
        coEvery { authManager.extractIdToken(any()) } returns "test_token"
        coEvery { authManager.authenticateWithFirebase(any()) } returns authResult
        coEvery { authManager.getFirebaseUser(any()) } returns firebaseUser
        coEvery { authManager.getUserId(any()) } returns userId
        coEvery { userProfileManager.createInitialProfile(any(), any()) } returns mapOf("key" to "value")
        coEvery { userProfileMapper.convertMapToJson(any()) } returns userProfileJson
        coEvery { authManager.updateUserProfileJson(any()) } just Runs
        coEvery { remoteUserRepository.isUserRegistered(userId) } returns isRegistered
        coEvery { authStateHolder.setRequiresConsent(any(), any()) } just Runs
        coEvery { userProfileManager.syncExistingUser(any()) } just Runs
    }
}