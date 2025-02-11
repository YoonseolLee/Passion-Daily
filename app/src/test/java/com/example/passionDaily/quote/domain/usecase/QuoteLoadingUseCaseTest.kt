package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.quote.data.remote.RemoteQuoteRepository
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.util.MainCoroutineRule
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteLoadingUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var useCase: QuoteLoadingUseCase
    private lateinit var remoteQuoteRepository: RemoteQuoteRepository
    private lateinit var quoteStateHolder: QuoteStateHolder

    @Before
    fun setup() {
        remoteQuoteRepository = mockk()
        quoteStateHolder = mockk {
            coEvery { updateHasQuoteReachedEnd(any()) } just runs
            coEvery { addQuotes(any(), any()) } just runs
        }

        useCase = QuoteLoadingUseCase(remoteQuoteRepository, quoteStateHolder)
    }

    @Test
    fun `fetchQuotesByCategory는 정상적으로 데이터를 가져온다`() = mainCoroutineRule.runTest {
        // given
        val category = QuoteCategory.CONFIDENCE
        val pageSize = 10
        val lastLoadedQuote = mockk<DocumentSnapshot>()
        val expectedResult = QuoteResult(listOf(mockk()), mockk())

        coEvery {
            remoteQuoteRepository.getQuotesByCategory(category, pageSize, lastLoadedQuote)
        } returns expectedResult

        // when
        val result = useCase.fetchQuotesByCategory(category, pageSize, lastLoadedQuote)

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `loadQuotesBeforeTarget은 예외 발생시 빈 리스트를 반환한다`() = mainCoroutineRule.runTest {
        // given
        val quoteId = "test_id"
        val category = QuoteCategory.CONFIDENCE
        val limit = 10

        coEvery {
            remoteQuoteRepository.getQuotesBeforeId(category, quoteId, limit)
        } throws FirebaseFirestoreException("Test exception", FirebaseFirestoreException.Code.UNAVAILABLE)

        // when
        val result = useCase.loadQuotesBeforeTarget(quoteId, category, limit)

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `loadTargetQuote은 quote를 정상적으로 가져온다`() = mainCoroutineRule.runTest {
        // given
        val quoteId = "test_id"
        val category = QuoteCategory.CONFIDENCE
        val expectedQuote = mockk<Quote> {
            every { id } returns "test_id"
        }

        coEvery {
            remoteQuoteRepository.getQuoteById(quoteId, category)
        } returns expectedQuote

        // when
        val result = useCase.loadTargetQuote(quoteId, category)
        advanceUntilIdle()

        // then
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(expectedQuote)
    }

    @Test
    fun `shouldLoadMoreQuotes는 적절한 조건에서 true를 반환한다`() {
        // given
        val nextIndex = 10
        val currentQuotes = List<Quote>(5) { mockk() }
        val hasReachedEnd = MutableStateFlow(false)

        // when
        val result = useCase.shouldLoadMoreQuotes(nextIndex, currentQuotes, hasReachedEnd)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `updateQuotesAfterLoading은 빈 결과일 때 hasQuoteReachedEnd를 true로 설정한다`() = mainCoroutineRule.runTest {
        // given
        val emptyResult = QuoteResult(emptyList(), null)
        val lastLoadedQuote: (DocumentSnapshot?) -> Unit = mockk(relaxed = true)

        // when
        useCase.updateQuotesAfterLoading(emptyResult, lastLoadedQuote)

        // then
        coVerify { quoteStateHolder.updateHasQuoteReachedEnd(true) }
    }

    @Test
    fun `updateQuotesAfterLoading은 결과가 있을 때 quotes를 추가한다`() = mainCoroutineRule.runTest {
        // given
        val quotes = listOf<Quote>(mockk(), mockk())
        val lastDocument = mockk<DocumentSnapshot>()
        val result = QuoteResult(quotes, lastDocument)
        val lastLoadedQuote: (DocumentSnapshot?) -> Unit = mockk(relaxed = true)

        // when
        useCase.updateQuotesAfterLoading(result, lastLoadedQuote)

        // then
        coVerify {
            quoteStateHolder.addQuotes(quotes, false)
            lastLoadedQuote(lastDocument)
        }
    }
}
