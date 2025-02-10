package com.example.passionDaily.quotecategory.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CategoryScreenTest {
    private val mainCoroutineRule = MainCoroutineRule()
    private val composeTestRule = createComposeRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(mainCoroutineRule)
        .around(composeTestRule)

    private val quoteViewModel = mockk<QuoteViewModel>(relaxed = true)
    private val quoteStateHolder = mockk<QuoteStateHolder>(relaxed = true)

    @Before
    fun setup() {
        every { quoteStateHolder.categories } returns MutableStateFlow(
            QuoteCategory.values().map { it.koreanName }
        )
        every { quoteStateHolder.selectedQuoteCategory } returns MutableStateFlow(QuoteCategory.EFFORT)
    }

    @Test
    fun 카테고리_화면이_올바르게_표시된다() = mainCoroutineRule.runTest {
        // Given & When
        composeTestRule.setContent {
            CategoryScreen(
                quoteViewModel = quoteViewModel,
                quoteStateHolder = quoteStateHolder,
                onNavigateToQuote = {},
                onBack = {}
            )
        }

        // Then
        composeTestRule.waitForIdle()

        // 제목 확인
        composeTestRule
            .onNodeWithText("카테고리", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()

        // 가이드 텍스트 확인
        composeTestRule
            .onNodeWithText("오늘은 어떤 주제의 명언을 만나볼까요?", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun 카테고리_선택시_선택된_카테고리로_업데이트되고_명언화면으로_이동한다() = mainCoroutineRule.runTest {
        // Given
        val onNavigateToQuote = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CategoryScreen(
                quoteViewModel = quoteViewModel,
                quoteStateHolder = quoteStateHolder,
                onNavigateToQuote = onNavigateToQuote,
                onBack = {}
            )
        }

        // When
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("사랑")
            .performClick()

        // Then
        verify {
            quoteViewModel.onCategorySelected(QuoteCategory.LOVE)
            onNavigateToQuote()
        }
    }

    @Test
    fun 뒤로가기_버튼_클릭시_이전화면으로_이동한다() = mainCoroutineRule.runTest {
        // Given
        val onBack = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CategoryScreen(
                quoteViewModel = quoteViewModel,
                quoteStateHolder = quoteStateHolder,
                onNavigateToQuote = {},
                onBack = onBack
            )
        }

        composeTestRule.waitForIdle()

        // When
        composeTestRule
            .onNodeWithContentDescription("Back", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Then
        verify { onBack() }
    }
}