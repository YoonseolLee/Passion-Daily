package com.example.passionDaily.favorites.presentation.screen

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
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.util.MainCoroutineRule
import org.junit.rules.RuleChain
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import org.junit.Before

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockFavoritesViewModel = mockk<FavoritesViewModel>(relaxed = true)
    private val mockQuoteViewModel = mockk<QuoteViewModel>(relaxed = true)
    private val mockNavigateToFavorites = mockk<() -> Unit>()
    private val mockNavigateToQuote = mockk<() -> Unit>()
    private val mockNavigateToSettings = mockk<() -> Unit>()
    private val mockNavigateToLogin = mockk<() -> Unit>()

    private val favoriteQuotesFlow = MutableStateFlow<List<QuoteEntity>>(emptyList())
    private val currentFavoriteQuoteFlow = MutableStateFlow<QuoteEntity?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    private val 테스트명언 = QuoteEntity(
        quoteId = "1",
        categoryId = 1,
        text = "테스트 명언",
        person = "테스트 인물",
        imageUrl = "test_url"
    )

    @Before
    fun setUp() {
        every { mockFavoritesViewModel.favoriteQuotes } returns favoriteQuotesFlow
        every { mockFavoritesViewModel.currentFavoriteQuote } returns currentFavoriteQuoteFlow
        every { mockFavoritesViewModel.isFavoriteLoading } returns isLoadingFlow
    }

    @Test
    fun Favorites_화면_진입시_즐겨찾기_목록을_로드한다() = mainCoroutineRule.runTest {
        // When
        composeTestRule.setContent {
            FavoritesScreen(
                favoritesViewModel = mockFavoritesViewModel,
                quoteViewModel = mockQuoteViewModel,
                onNavigateToFavorites = mockNavigateToFavorites,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToSettings = mockNavigateToSettings,
                onNavigateToLogin = mockNavigateToLogin,
                currentScreen = NavigationBarScreens.FAVORITES
            )
        }

        // Then
        verify { mockFavoritesViewModel.loadFavorites() }
    }

    @Test
    fun 즐겨찾기가_비어있을때_안내_메시지가_표시된다() = mainCoroutineRule.runTest {
        // Given
        favoriteQuotesFlow.value = emptyList()
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                favoritesViewModel = mockFavoritesViewModel,
                quoteViewModel = mockQuoteViewModel,
                onNavigateToFavorites = mockNavigateToFavorites,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToSettings = mockNavigateToSettings,
                onNavigateToLogin = mockNavigateToLogin,
                currentScreen = NavigationBarScreens.FAVORITES
            )
        }

        // Then
        composeTestRule.onNodeWithText("아직 즐겨찾기한 명언이 없어요.").assertExists()
    }

    @Test
    fun 로딩중일때_프로그레스_인디케이터가_표시된다() = mainCoroutineRule.runTest {
        // Given
        isLoadingFlow.value = true

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                favoritesViewModel = mockFavoritesViewModel,
                quoteViewModel = mockQuoteViewModel,
                onNavigateToFavorites = mockNavigateToFavorites,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToSettings = mockNavigateToSettings,
                onNavigateToLogin = mockNavigateToLogin,
                currentScreen = NavigationBarScreens.FAVORITES
            )
        }

        // Then
        composeTestRule.onNodeWithTag("LoadingIndicator").assertExists()
    }

    @Test
    fun 다음_버튼_클릭시_다음_명언으로_이동한다() = mainCoroutineRule.runTest {
        // Given
        favoriteQuotesFlow.value = listOf(테스트명언)
        currentFavoriteQuoteFlow.value = 테스트명언
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                favoritesViewModel = mockFavoritesViewModel,
                quoteViewModel = mockQuoteViewModel,
                onNavigateToFavorites = mockNavigateToFavorites,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToSettings = mockNavigateToSettings,
                onNavigateToLogin = mockNavigateToLogin,
                currentScreen = NavigationBarScreens.FAVORITES
            )
        }

        composeTestRule.onNodeWithTag("RightArrow").performClick()

        // Then
        verify { mockFavoritesViewModel.nextQuote() }
    }

    @Test
    fun 이전_버튼_클릭시_이전_명언으로_이동한다() = mainCoroutineRule.runTest {
        // Given
        favoriteQuotesFlow.value = listOf(테스트명언)
        currentFavoriteQuoteFlow.value = 테스트명언
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                favoritesViewModel = mockFavoritesViewModel,
                quoteViewModel = mockQuoteViewModel,
                onNavigateToFavorites = mockNavigateToFavorites,
                onNavigateToQuote = mockNavigateToQuote,
                onNavigateToSettings = mockNavigateToSettings,
                onNavigateToLogin = mockNavigateToLogin,
                currentScreen = NavigationBarScreens.FAVORITES
            )
        }

        composeTestRule.onNodeWithTag("LeftArrow").performClick()

        // Then
        verify { mockFavoritesViewModel.previousQuote() }
    }
}