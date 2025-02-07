package com.example.passionDaily.login.presentation.viewmodel


import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
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
import com.example.passionDaily.util.mapper.UserProfileMapper
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.apache.http.auth.AuthState
import org.junit.After
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

    private lateinit var viewModel: SharedLogInViewModel

    @Before
    fun setup() {

        every { loginStateHolder.isLoading } returns MutableStateFlow(false)
        every { authStateHolder.authState } returns MutableStateFlow(Unauthenticated)
        every { userConsentManager.consent } returns MutableStateFlow(
            UserConsent(
                termsOfService = false,
                privacyPolicy = false
            )
        )
        every { userConsentManager.isAgreeAllChecked } returns MutableStateFlow(false)

        coEvery { authManager.clearCredentials() } just Runs
        coEvery { authManager.startLoading() } just Runs
        coEvery { authManager.stopLoading() } just Runs

        viewModel = SharedLogInViewModel(
            authManager = authManager,
            userProfileManager = userProfileManager,
            userConsentManager = userConsentManager,
            urlManager = mockk(),
            remoteUserRepository = remoteUserRepository,
            toastManager = mockk(),
            userProfileMapper = userProfileMapper,
            stringProvider = mockk(),
            authStateHolder = authStateHolder,
            loginStateHolder = loginStateHolder
        )
    }

}