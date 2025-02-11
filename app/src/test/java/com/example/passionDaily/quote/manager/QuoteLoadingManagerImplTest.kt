package com.example.passionDaily.quote.manager

import com.example.passionDaily.constants.ManagerConstants.QuoteLoading.PAGE_SIZE
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.domain.usecase.QuoteListManagementUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteLoadingUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteStateManagementUseCase
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class QuoteLoadingManagerImplTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val stateManagementUseCase: QuoteStateManagementUseCase = mockk(relaxed = true)
    private val loadingUseCase: QuoteLoadingUseCase = mockk(relaxed = true)
    private val listManagementUseCase: QuoteListManagementUseCase = mockk(relaxed = true)

    private lateinit var manager: QuoteLoadingManagerImpl

    @Before
    fun setUp() {
        manager = QuoteLoadingManagerImpl(
            stateManagementUseCase,
            loadingUseCase,
            listManagementUseCase
        )
    }

    @Test
    fun `명언 로딩 시작하면 상태가 true로 변경`() = mainCoroutineRule.runTest {
        // given
        coEvery { stateManagementUseCase.updateIsQuoteLoading(true) } just Runs

        // when
        manager.startQuoteLoading()

        // then
        coVerify { stateManagementUseCase.updateIsQuoteLoading(true) }
    }

    @Test
    fun `카테고리별 명언 불러오면 올바른 결과 반환`() = mainCoroutineRule.runTest {
        // given
        val category = QuoteCategory.CONFIDENCE
        val pageSize = 10
        val lastLoadedQuote: DocumentSnapshot? = mockk()
        val expectedResult: QuoteResult = mockk()
        coEvery { loadingUseCase.fetchQuotesByCategory(category, pageSize, lastLoadedQuote) } returns expectedResult

        // when
        val result = manager.fetchQuotesByCategory(category, pageSize, lastLoadedQuote)

        // then
        assertThat(result).isEqualTo(expectedResult)
        coVerify { loadingUseCase.fetchQuotesByCategory(category, pageSize, lastLoadedQuote) }
    }

    @Test
    fun `마지막 명언 도달 상태를 true로 설정`() = mainCoroutineRule.runTest {
        // given
        coEvery { stateManagementUseCase.updateHasQuoteReachedEnd(true) } just Runs

        // when
        manager.setHasQuoteReachedEndTrue()

        // then
        coVerify { stateManagementUseCase.updateHasQuoteReachedEnd(true) }
    }

    @Test
    fun `명언을 상태에 추가`() = mainCoroutineRule.runTest {
        // given
        val quotes: List<Quote> = mockk()
        val isNewCategory = true
        coEvery { stateManagementUseCase.addQuotes(quotes, isNewCategory) } just Runs

        // when
        manager.addQuotesToState(quotes, isNewCategory)

        // then
        coVerify { stateManagementUseCase.addQuotes(quotes, isNewCategory) }
    }

    @Test
    fun `특정 명언 이전 명언 불러오기`() = mainCoroutineRule.runTest {
        // given
        val quoteId = "test_quote"
        val category = QuoteCategory.CONFIDENCE
        val expectedQuotes: List<Quote> = listOf(mockk())
        coEvery { loadingUseCase.loadQuotesBeforeTarget(quoteId, category, PAGE_SIZE) } returns expectedQuotes

        // when
        val result = manager.loadQuotesBeforeTarget(quoteId, category)

        // then
        assertThat(result).isEqualTo(expectedQuotes)
        coVerify { loadingUseCase.loadQuotesBeforeTarget(quoteId, category, PAGE_SIZE) }
    }
}
