package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@ExperimentalCoroutinesApi
class QuoteListManagementUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var useCase: QuoteListManagementUseCase
    private lateinit var quoteStateHolder: QuoteStateHolder

    @Before
    fun setup() {
        quoteStateHolder = mockk {
            coEvery { clearQuotes() } just runs
            coEvery { addQuotes(any(), any()) } just runs
            coEvery { updateSelectedCategory(any()) } just runs
        }
        useCase = QuoteListManagementUseCase(quoteStateHolder)
    }

    @Test
    fun `clearExistingQuotes는 QuoteStateHolder의 clearQuotes를 호출한다`() = mainCoroutineRule.runTest {
        // when
        useCase.clearExistingQuotes()

        // then
        coVerify { quoteStateHolder.clearQuotes() }
    }

    @Test
    fun `addInitialQuotes는 이전 quote들과 타겟 quote를 합쳐서 추가한다`() = mainCoroutineRule.runTest {
        // given
        val beforeQuotes = listOf(
            mockk<Quote>(relaxed = true),
            mockk<Quote>(relaxed = true)
        )
        val targetQuote = mockk<Quote>(relaxed = true)

        // when
        useCase.addInitialQuotes(beforeQuotes, targetQuote)

        // then
        coVerify {
            quoteStateHolder.addQuotes(
                match { quotes ->
                    quotes.size == 3 &&
                            quotes.take(2) == beforeQuotes &&
                            quotes.last() == targetQuote
                },
                true
            )
        }
    }

    @Test
    fun `addAfterQuotes는 결과가 비어있지 않을 때만 quotes를 추가한다`() = mainCoroutineRule.runTest {
        // given
        val quotes = listOf(mockk<Quote>())
        val result = QuoteResult(quotes, mockk())

        // when
        useCase.addAfterQuotes(result)

        // then
        coVerify { quoteStateHolder.addQuotes(quotes, false) }
    }

    @Test
    fun `addAfterQuotes는 결과가 비어있을 때 quotes를 추가하지 않는다`() = mainCoroutineRule.runTest {
        // given
        val emptyResult = QuoteResult(emptyList(), mockk())

        // when
        useCase.addAfterQuotes(emptyResult)

        // then
        coVerify(exactly = 0) { quoteStateHolder.addQuotes(any(), any()) }
    }

    @Test
    fun `getUpdatedLastLoadedQuote는 입력된 document를 그대로 반환한다`() {
        // given
        val document = mockk<DocumentSnapshot>()

        // when
        val result = useCase.getUpdatedLastLoadedQuote(document)

        // then
        assertThat(result).isEqualTo(document)
    }

    @Test
    fun `updateSelectedCategory는 QuoteStateHolder의 updateSelectedCategory를 호출한다`() =
        mainCoroutineRule.runTest {
            // given
            val category = QuoteCategory.CREATIVITY

            // when
            useCase.updateSelectedCategory(category)

            // then
            coVerify { quoteStateHolder.updateSelectedCategory(category) }
        }
}