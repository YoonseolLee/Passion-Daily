package com.example.passionDaily.favorites.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.passionDaily.constants.ViewModelConstants.Favorites.KEY_FAVORITE_INDEX
import com.example.passionDaily.favorites.manager.FavoritesLoadingManager
import com.example.passionDaily.favorites.manager.FavoritesRemoveManager
import com.example.passionDaily.favorites.manager.FavoritesSavingManager
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolderImpl
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FavoritesViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var quoteStateHolder: QuoteStateHolder
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var favoritesStateHolder: FavoritesStateHolderImpl
    private lateinit var favoritesLoadingManager: FavoritesLoadingManager
    private lateinit var favoritesSavingManager: FavoritesSavingManager
    private lateinit var favoritesRemoveManager: FavoritesRemoveManager
    private lateinit var toastManager: ToastManager
    private lateinit var mockUser: FirebaseUser


    @Before
    fun setUp() {
        quoteStateHolder = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()
        firebaseAuth = mockk(relaxed = true)
        favoritesStateHolder = FavoritesStateHolderImpl()
        favoritesLoadingManager = mockk(relaxed = true)
        favoritesSavingManager = mockk(relaxed = true)
        favoritesRemoveManager = mockk(relaxed = true)
        toastManager = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)

        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "testUserId"

        viewModel = FavoritesViewModel(
            quoteStateHolder = quoteStateHolder,
            savedStateHandle = savedStateHandle,
            favoritesStateHolder = favoritesStateHolder,
            favoritesLoadingManager = favoritesLoadingManager,
            favoritesSavingManager = favoritesSavingManager,
            favoritesRemoveManager = favoritesRemoveManager,
            toastManager = toastManager,
        )
    }

    @Test
    fun `다음_즐겨찾기_이동시_정상적으로_인덱스가_증가한다`() = mainCoroutineRule.runTest {
        // given
        val quotes = listOf(
            QuoteEntity(
                quoteId = "1",
                text = "명언1",
                person = "인물1",
                imageUrl = "url1",
                categoryId = 1
            ),
            QuoteEntity(
                quoteId = "2",
                text = "명언2",
                person = "인물2",
                imageUrl = "url2",
                categoryId = 1
            ),
            QuoteEntity(
                quoteId = "3",
                text = "명언3",
                person = "인물3",
                imageUrl = "url3",
                categoryId = 1
            )
        )
        favoritesStateHolder.updateFavoriteQuotes(quotes)

        // when
        viewModel.nextQuote()

        // then
        assertThat(viewModel.currentFavoriteQuote.first()?.quoteId).isEqualTo("2")
    }

    @Test
    fun `이전_즐겨찾기_이동시_정상적으로_인덱스가_감소한다`() = mainCoroutineRule.runTest {
        // given
        val quotes = listOf(
            QuoteEntity(
                quoteId = "1",
                text = "명언1",
                person = "인물1",
                imageUrl = "url1",
                categoryId = 1
            ),
            QuoteEntity(
                quoteId = "2",
                text = "명언2",
                person = "인물2",
                imageUrl = "url2",
                categoryId = 1
            )
        )
        favoritesStateHolder.updateFavoriteQuotes(quotes)
        savedStateHandle[KEY_FAVORITE_INDEX] = 1

        // when
        viewModel.previousQuote()

        // then
        assertThat(viewModel.currentFavoriteQuote.first()?.quoteId).isEqualTo("1")
    }

    @Test
    fun `즐겨찾기_삭제시_로컬DB에서_정상적으로_삭제된다`() = mainCoroutineRule.runTest {

    }

    @Test
    fun `로딩_상태가_정상적으로_업데이트된다`() = mainCoroutineRule.runTest {
        // given
        favoritesStateHolder.updateIsFavoriteLoading(false)

        // when
        favoritesStateHolder.updateIsFavoriteLoading(true)

        // then
        assertThat(favoritesStateHolder.isFavoriteLoading.first()).isTrue()
    }

    @Test
    fun `에러_메시지가_정상적으로_업데이트된다`() = mainCoroutineRule.runTest {
        // given
        val errorMessage = "테스트 에러"

        // when
        favoritesStateHolder.updateError(errorMessage)

        // then
        assertThat(favoritesStateHolder.error.first()).isEqualTo(errorMessage)
    }
}