package com.example.passionDaily.settings.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.settings.presentation.viewmodel.SettingsViewModel
import com.example.passionDaily.util.MainCoroutineRule
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.rules.RuleChain
import java.time.LocalTime

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    private val mainCoroutineRule = MainCoroutineRule()
    private val composeTestRule = createComposeRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(mainCoroutineRule)
        .around(composeTestRule)

    private val settingsViewModel = mockk<SettingsViewModel>(relaxed = true)
    private val mockUser = mockk<FirebaseUser>(relaxed = true)
    private val defaultTime = LocalTime.of(8, 0)

    @Before
    fun setup() {
        every { settingsViewModel.currentUser } returns MutableStateFlow(null)
        every { settingsViewModel.notificationEnabled } returns MutableStateFlow(false)
        every { settingsViewModel.notificationTime } returns MutableStateFlow(defaultTime)
        every { settingsViewModel.showWithdrawalDialog } returns MutableStateFlow(false)
        every { settingsViewModel.isLoading } returns MutableStateFlow(false)
    }

    @Test
    fun 설정화면_기본요소들이_표시된다() = mainCoroutineRule.runTest {
        // Given & When
        composeTestRule.setContent {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToFavorites = {},
                onNavigateToQuote = {},
                onNavigateToSettings = {},
                onNavigateToLogin = {},
                currentScreen = NavigationBarScreens.SETTINGS,
                onBack = {}
            )
        }

        composeTestRule.waitForIdle()

        // Then
        // Box의 정중앙에 있는 "설정" 텍스트 확인
        composeTestRule
            .onNodeWithTag("SettingsTitle")
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("설정")

        // 카테고리 헤더들 확인
        listOf("알림 설정", "계정 관리", "고객 지원", "약관 및 개인정보 처리 동의").forEach { category ->
            composeTestRule
                .onNode(
                    hasText(category) and
                            hasParent(hasTestTag("CategoryHeader"))
                )
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun 로그인되지_않은_상태에서는_로그인_버튼이_표시된다() = mainCoroutineRule.runTest {
        // Given
        every { settingsViewModel.currentUser } returns MutableStateFlow(null)

        // When
        composeTestRule.setContent {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToFavorites = {},
                onNavigateToQuote = {},
                onNavigateToSettings = {},
                onNavigateToLogin = {},
                currentScreen = NavigationBarScreens.SETTINGS,
                onBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("로그인").assertExists()
        composeTestRule.onNodeWithText("로그아웃").assertDoesNotExist()
    }

    @Test
    fun 로그인된_상태에서는_로그아웃_버튼이_표시된다() = mainCoroutineRule.runTest {
        // Given
        every { settingsViewModel.currentUser } returns MutableStateFlow(mockUser)

        // When
        composeTestRule.setContent {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToFavorites = {},
                onNavigateToQuote = {},
                onNavigateToSettings = {},
                onNavigateToLogin = {},
                currentScreen = NavigationBarScreens.SETTINGS,
                onBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("로그아웃").assertExists()
        composeTestRule.onNodeWithText("로그인").assertDoesNotExist()
    }

    @Test
    fun 뒤로가기_버튼_클릭시_이전화면으로_이동한다() = mainCoroutineRule.runTest {
        // Given
        val onBack = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToFavorites = {},
                onNavigateToQuote = {},
                onNavigateToSettings = {},
                onNavigateToLogin = {},
                currentScreen = NavigationBarScreens.SETTINGS,
                onBack = onBack
            )
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Then
        verify { onBack() }
    }

    @Test
    fun 로딩중일때_프로그레스바가_표시된다() = mainCoroutineRule.runTest {
        // Given
        every { settingsViewModel.isLoading } returns MutableStateFlow(true)

        // When
        composeTestRule.setContent {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToFavorites = {},
                onNavigateToQuote = {},
                onNavigateToSettings = {},
                onNavigateToLogin = {},
                currentScreen = NavigationBarScreens.SETTINGS,
                onBack = {}
            )
        }

        // Then
        composeTestRule
            .onNode(hasTestTag("LoadingIndicator"))
            .assertExists()
    }
}