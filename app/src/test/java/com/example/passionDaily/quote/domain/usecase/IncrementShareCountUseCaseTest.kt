package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.quote.data.remote.repository.RemoteQuoteRepository
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncrementShareCountUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var useCase: IncrementShareCountUseCase
    private lateinit var remoteQuoteRepository: RemoteQuoteRepository

    @Before
    fun setup() {
        remoteQuoteRepository = mockk {
            coEvery { incrementShareCount(any(), any()) } returns Unit
        }
        useCase = IncrementShareCountUseCase(remoteQuoteRepository)
    }

    @Test
    fun `공유 횟수를 증가시킬 때 repository를 호출한다`() = mainCoroutineRule.runTest {
        // given
        val quoteId = "test_quote_id"
        val category = QuoteCategory.LOVE

        // when
        useCase.incrementShareCount(quoteId, category)
        advanceUntilIdle()

        // then
        coVerify { remoteQuoteRepository.incrementShareCount(quoteId, category) }
    }

    @Test
    fun `카테고리가 null이면 repository를 호출하지 않는다`() = mainCoroutineRule.runTest {
        // given
        val quoteId = "test_quote_id"

        // when
        useCase.incrementShareCount(quoteId, null)
        advanceUntilIdle()

        // then
        coVerify(exactly = 0) { remoteQuoteRepository.incrementShareCount(any(), any()) }
    }
}