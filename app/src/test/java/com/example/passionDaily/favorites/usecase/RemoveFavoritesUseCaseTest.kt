package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RemoveFavoritesUseCaseTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var removeFavoritesUseCase: RemoveFavoritesUseCase
    private val localFavoriteRepository: LocalFavoriteRepository = mockk()

    @Before
    fun setup() {
        removeFavoritesUseCase = RemoveFavoritesUseCase(localFavoriteRepository)
    }

    @Test
    fun `즐겨찾기 삭제 후 남은 즐겨찾기 목록을 반환한다`() = mainCoroutineRule.runTest {
        // Given
        val quoteId = "quote1"
        val categoryId = 1
        val favoriteEntity = mockk<FavoriteEntity>()
        val expectedFavorites = listOf(favoriteEntity)

        coEvery { localFavoriteRepository.deleteFavorite(quoteId, categoryId) } just Runs
        coEvery { localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId) } returns expectedFavorites

        // When
        val result = removeFavoritesUseCase.deleteLocalFavorite(quoteId, categoryId)

        // Then
        coVerify {
            localFavoriteRepository.deleteFavorite(quoteId, categoryId)
            localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId)
        }
        assertThat(result).isEqualTo(expectedFavorites)
    }

    @Test
    fun `즐겨찾기 삭제 시 에러가 발생하면 예외를 던진다`() = mainCoroutineRule.runTest {
        // Given
        val quoteId = "quote1"
        val categoryId = 1
        val exception = RuntimeException("Delete failed")

        coEvery { localFavoriteRepository.deleteFavorite(quoteId, categoryId) } throws exception

        // When & Then
        val thrown = assertThrows(RuntimeException::class.java) {
            runBlocking {
                removeFavoritesUseCase.deleteLocalFavorite(quoteId, categoryId)
            }
        }

        assertThat(thrown.message).isEqualTo("Delete failed")
        coVerify { localFavoriteRepository.deleteFavorite(quoteId, categoryId) }
    }

    @Test
    fun `남은 즐겨찾기 조회 시 에러가 발생하면 예외를 던진다`() = mainCoroutineRule.runTest {
        // Given
        val quoteId = "quote1"
        val categoryId = 1
        val exception = RuntimeException("Query failed")

        coEvery { localFavoriteRepository.deleteFavorite(quoteId, categoryId) } just Runs
        coEvery { localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId) } throws exception

        // When & Then
        val thrown = assertThrows(RuntimeException::class.java) {
            runBlocking {
                removeFavoritesUseCase.deleteLocalFavorite(quoteId, categoryId)
            }
        }

        assertThat(thrown.message).isEqualTo("Query failed")
        coVerify {
            localFavoriteRepository.deleteFavorite(quoteId, categoryId)
            localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId)
        }
    }
}