package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.usecase.SaveFavoritesToLocalUseCase
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepository
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SaveFavoritesToLocalUseCaseTest {
    private lateinit var useCase: SaveFavoritesToLocalUseCase
    private val localFavoriteRepository: LocalFavoriteRepository = mockk()
    private val localQuoteRepository: LocalQuoteRepository = mockk()
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository = mockk()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        useCase = SaveFavoritesToLocalUseCase(
            localFavoriteRepository,
            localQuoteRepository,
            localQuoteCategoryRepository
        )
    }

    @Test
    fun `새로운 카테고리와 인용구를 저장할 때 모든 단계가 정상적으로 실행된다`() = mainCoroutineRule.runTest {
        // Given
        val category = QuoteCategory.CREATIVITY
        val quote = Quote(
            id = "1",
            category = category,
            text = "test quote",
            person = "author",
            imageUrl = "url",
            createdAt = "2024-02-24",
            modifiedAt = "2024-02-24",
            shareCount = 0
        )

        coEvery { localQuoteCategoryRepository.isCategoryExists(any()) } returns false
        coEvery { localQuoteCategoryRepository.insertCategory(any()) } just Runs
        coEvery { localQuoteRepository.isQuoteExistsInCategory(any(), any()) } returns false
        coEvery { localQuoteRepository.insertQuote(any()) } just Runs
        coEvery { localFavoriteRepository.insertFavorite(any()) } just Runs

        // When
        useCase.saveToLocalDatabase(category, quote)

        // Then
        coVerify {
            localQuoteCategoryRepository.insertCategory(any())
            localQuoteRepository.insertQuote(any())
            localFavoriteRepository.insertFavorite(any())
        }
    }
}