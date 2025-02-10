package com.example.passionDaily.termsConsent.presentation.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.passionDaily.login.domain.model.UserConsent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.passionDaily.login.presentation.viewmodel.SharedLogInViewModel
import com.example.passionDaily.util.MainCoroutineRule
import org.junit.Before
import org.junit.rules.RuleChain

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TermsConsentScreenTest {
    private val mainCoroutineRule = MainCoroutineRule()
    private val composeTestRule = createComposeRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(mainCoroutineRule)
        .around(composeTestRule)

    private val sharedLogInViewModel = mockk<SharedLogInViewModel>(relaxed = true)

    @Before
    fun setup() {
        every { sharedLogInViewModel.isAgreeAllChecked } returns MutableStateFlow(false)
        every { sharedLogInViewModel.consent } returns MutableStateFlow(UserConsent(false, false))
        every { sharedLogInViewModel.userProfileJsonV2 } returns MutableStateFlow(null)
    }

    @Test
    fun 모든_필수요소가_화면에_표시된다() = mainCoroutineRule.runTest {
        // Given & When
        composeTestRule.setContent {
            TermsConsentScreen(
                userProfileJson = null,
                sharedLogInViewModel = sharedLogInViewModel,
                onNavigateToQuoteScreen = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("이용 약관 동의")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("전체 동의하기")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("이용약관")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("개인정보 수집 및 이용 동의")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("다음")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun 전체동의_체크시_모든_약관이_체크된다() = mainCoroutineRule.runTest {
        // Given
        composeTestRule.setContent {
            TermsConsentScreen(
                userProfileJson = null,
                sharedLogInViewModel = sharedLogInViewModel,
                onNavigateToQuoteScreen = {}
            )
        }

        // When
        composeTestRule
            .onNodeWithText("전체 동의하기")
            .performClick()

        // Then
        verify { sharedLogInViewModel.toggleAgreeAll() }
    }

    @Test
    fun 필수약관_모두_동의시_다음버튼_활성화된다() = mainCoroutineRule.runTest {
        // Given
        every { sharedLogInViewModel.consent } returns MutableStateFlow(UserConsent(true, true))

        composeTestRule.setContent {
            TermsConsentScreen(
                userProfileJson = null,
                sharedLogInViewModel = sharedLogInViewModel,
                onNavigateToQuoteScreen = {}
            )
        }

        // Then
        composeTestRule
            .onNode(
                hasText("다음") and
                        hasClickAction()
            )
            .assertIsEnabled()
    }

    @Test
    fun 필수약관_미동의시_다음버튼_비활성화된다() = mainCoroutineRule.runTest {
        // Given
        every { sharedLogInViewModel.consent } returns MutableStateFlow(UserConsent(false, false))

        composeTestRule.setContent {
            TermsConsentScreen(
                userProfileJson = null,
                sharedLogInViewModel = sharedLogInViewModel,
                onNavigateToQuoteScreen = {}
            )
        }

        // Then
        composeTestRule
            .onNode(
                hasText("다음") and
                        hasClickAction()
            )
            .assertIsNotEnabled()
    }

    @Test
    fun 다음버튼_클릭시_명언화면으로_이동한다() = mainCoroutineRule.runTest {
        // Given
        val onNavigateToQuoteScreen = mockk<() -> Unit>(relaxed = true)
        every { sharedLogInViewModel.consent } returns MutableStateFlow(UserConsent(true, true))
        every { sharedLogInViewModel.userProfileJsonV2 } returns MutableStateFlow("test_profile")

        composeTestRule.setContent {
            TermsConsentScreen(
                userProfileJson = "test_profile",
                sharedLogInViewModel = sharedLogInViewModel,
                onNavigateToQuoteScreen = onNavigateToQuoteScreen
            )
        }

        // When
        composeTestRule
            .onNodeWithText("다음")
            .performClick()

        // Then
        verify {
            sharedLogInViewModel.handleNextClick("test_profile")
            onNavigateToQuoteScreen()
        }
    }
}