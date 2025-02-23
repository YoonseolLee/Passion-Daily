package com.example.passionDaily.quote.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class QuoteScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val quoteViewModel = mockk<QuoteViewModel>(relaxed = true)
    private val favoritesViewModel = mockk<FavoritesViewModel>(relaxed = true)
    private val quoteStateHolder = mockk<QuoteStateHolder>(relaxed = true)
    private val navigateToCategory = mockk<() -> Unit>(relaxed = true)

    private val testQuote = Quote(
        id = "1",
        text = "테스트 명언",
        person = "테스트 작성자",
        imageUrl = "",
        category = QuoteCategory.CONFIDENCE,
        createdAt = "2020-12-12",
        modifiedAt = "2022-12-12",
        shareCount = 2
    )

    @Before
    fun setup() {
        every { quoteStateHolder.selectedQuoteCategory } returns MutableStateFlow(QuoteCategory.CONFIDENCE)
        every { quoteStateHolder.quotes } returns MutableStateFlow(listOf(testQuote))
        every { quoteStateHolder.isQuoteLoading } returns MutableStateFlow(false)
        every { quoteViewModel.currentQuote } returns MutableStateFlow(testQuote)
        every { quoteViewModel.quotes } returns MutableStateFlow(listOf(testQuote))
        every { quoteViewModel.isLoading } returns MutableStateFlow(false)


        composeTestRule.setContent {
            QuoteScreen(
                quoteViewModel = quoteViewModel,
                favoritesViewModel = favoritesViewModel,
                quoteStateHolder = quoteStateHolder,
                onNavigateToCategory = navigateToCategory,
                onNavigateToFavorites = { },
                onNavigateToQuote = { },
                onNavigateToSettings = { },
                currentScreen = NavigationBarScreens.QUOTE
            )
        }
    }

    @Test
    fun 카테고리선택_버튼_클릭시_카테고리화면으로_이동한다() {
        // When: 카테고리 버튼 클릭
        composeTestRule
            .onNodeWithText(QuoteCategory.CONFIDENCE.koreanName)
            .performClick()

        // Then: 네비게이션 콜백이 호출되었는지 검증
        verify(exactly = 1) { navigateToCategory() }
    }



    @Test
    fun 이전_버튼_클릭시_이전_명언으로_이동한다() {
        // When
        composeTestRule
            .onNodeWithTag("LeftArrow")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Then
        verify { quoteViewModel.previousQuote() }
    }

    @Test
    fun 다음_버튼_클릭시_다음_명언으로_이동한다() {
        // When
        composeTestRule
            .onNodeWithTag("RightArrow")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Then
        verify { quoteViewModel.nextQuote() }
    }

    @Test
    fun 명언이_있을때_명언이_표시된다() {
        // Then
        composeTestRule
            .onNodeWithText("테스트 명언")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("-테스트 작성자-")
            .assertExists()
            .assertIsDisplayed()
    }
}