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

    @Before
    fun setup() {
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
        listOf("고객 지원", "약관 및 개인정보 처리 동의").forEach { category ->
            composeTestRule
                .onNode(
                    hasText(category) and
                            hasParent(hasTestTag("CategoryHeader"))
                )
                .assertExists()
                .assertIsDisplayed()
        }
    }
}