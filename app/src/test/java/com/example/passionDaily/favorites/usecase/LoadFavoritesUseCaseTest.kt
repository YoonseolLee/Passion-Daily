package com.example.passionDaily.favorites.usecase

import app.cash.turbine.test
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalMultiplatform
class LoadFavoritesUseCaseTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var loadFavoritesUseCase: LoadFavoritesUseCase
    private val localFavoriteRepository: LocalFavoriteRepository = mockk()
    private val favoritesStateHolder: FavoritesStateHolder = mockk()

    @Before
    fun setUp() {
        loadFavoritesUseCase = LoadFavoritesUseCase(
            favoritesStateHolder,
            localFavoriteRepository
        )
    }

    @Test
    fun `즐겨찾기 확인 시 즐겨찾기 존재하고 아이디가 일치하면 true 반환`() =
        mainCoroutineRule.runTest {
            // Given
            val userId = "test_user"
            val quoteId = "test_quote"
            val categoryId = 1

            val favoriteEntity = FavoriteEntity(
                userId = userId,
                quoteId = quoteId,
                categoryId = categoryId
            )

            coEvery {
                localFavoriteRepository.checkFavoriteEntity(
                    userId,
                    quoteId,
                    categoryId
                )
            } returns flowOf(favoriteEntity)

            // When
            loadFavoritesUseCase.checkIfQuoteIsFavorite(userId, quoteId, categoryId).test {
                // Then
                assertThat(awaitItem()).isTrue()
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `즐겨찾기 확인 시 즐겨찾기 존재하지 않거나 아이디가 일치하지 않으면 false 반환`() =
        mainCoroutineRule.runTest {
            // Given
            val userId = "test_user"
            val quoteId = "test_quote"
            val categoryId = 1

            coEvery {
                localFavoriteRepository.checkFavoriteEntity(
                    userId,
                    quoteId,
                    categoryId
                )
            } returns flowOf(null)

            // When
            loadFavoritesUseCase.checkIfQuoteIsFavorite(userId, quoteId, categoryId).test {
                // Then
                assertThat(awaitItem()).isFalse()
                cancelAndConsumeRemainingEvents()
            }
        }
}
