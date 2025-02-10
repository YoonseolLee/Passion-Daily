package com.example.passionDaily.login.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.login.presentation.viewmodel.SharedLogInViewModel
import com.example.passionDaily.login.state.AuthState
import com.example.passionDaily.util.MainCoroutineRule
import org.junit.rules.RuleChain

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    private val mainCoroutineRule = MainCoroutineRule()
    private val composeTestRule = createComposeRule()

    @get:Rule
    val chain = RuleChain
        .outerRule(mainCoroutineRule)
        .around(composeTestRule)

    private val mockViewModel = mockk<SharedLogInViewModel>(relaxed = true)
    private val mockNavigateToQuote = mockk<() -> Unit>()
    private val mockNavigateToTermsConsent = mockk<(String) -> Unit>()

    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private val loadingStateFlow = MutableStateFlow(false)
    private val userProfileFlow = MutableStateFlow<String?>(null)

    init {
        every { mockViewModel.authState } returns authStateFlow
        every { mockViewModel.isLoading } returns loadingStateFlow
        every { mockViewModel.userProfileJson } returns userProfileFlow
    }

    @Test
    fun LoginScreenTest() = mainCoroutineRule.runTest {
        // Given
        composeTestRule.setContent {
            LoginScreen(
                sharedLogInViewModel = mockViewModel,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToTermsConsent = mockNavigateToTermsConsent
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("간편로그인 후 이용이 가능합니다.")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("구글로 로그인")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun clickGoogleLoginButton_callsSignInWithGoogle() = mainCoroutineRule.runTest {
        // Given
        composeTestRule.setContent {
            LoginScreen(
                sharedLogInViewModel = mockViewModel,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToTermsConsent = mockNavigateToTermsConsent
            )
        }

        // When
        composeTestRule
            .onNodeWithText("구글로 로그인")
            .performClick()

        advanceUntilIdle()

        // Then
        verify { mockViewModel.signInWithGoogle() }
    }

    @Test
    fun whenAuthStateChangesToAuthenticated_navigatesToQuote() = mainCoroutineRule.runTest {
        // Given
        every { mockNavigateToQuote.invoke() } returns Unit
        every { mockViewModel.signalLoginSuccess() } returns Unit

        composeTestRule.setContent {
            LoginScreen(
                sharedLogInViewModel = mockViewModel,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToTermsConsent = mockNavigateToTermsConsent
            )
        }

        // When
        authStateFlow.value = AuthState.Authenticated(userId = "test_user_id")

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                verify {
                    mockViewModel.signalLoginSuccess()
                    mockNavigateToQuote.invoke()
                }
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }


    @Test
    fun whenAuthStateChangesToRequiresConsent_navigatesToTermsConsent() = mainCoroutineRule.runTest {
        // Given
        val userProfileJson = """{"id": "123", "name": "Test User"}"""
        every { mockNavigateToTermsConsent.invoke(any()) } returns Unit

        composeTestRule.setContent {
            LoginScreen(
                sharedLogInViewModel = mockViewModel,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToTermsConsent = mockNavigateToTermsConsent
            )
        }

        // When
        userProfileFlow.value = userProfileJson
        authStateFlow.value = AuthState.RequiresConsent(
            userId = "test_user_id",
            userProfileJson = userProfileJson
        )

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                verify { mockNavigateToTermsConsent.invoke(userProfileJson) }
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}