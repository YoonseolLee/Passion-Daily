package com.example.passionDaily.quote.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.manager.QuoteLoadingManager
import com.example.passionDaily.quote.manager.ShareQuoteManager
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManager
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class QuoteViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: QuoteViewModel
    private val quoteStateHolder: QuoteStateHolder = mockk(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val categoryManager: QuoteCategoryManager = mockk()
    private val toastManager: ToastManager = mockk(relaxed = true)
    private val quoteLoadingManager: QuoteLoadingManager = mockk()
    private val shareQuoteManager: ShareQuoteManager = mockk()

    @Before
    fun setup() {
        every { quoteStateHolder.selectedQuoteCategory } returns MutableStateFlow(QuoteCategory.EFFORT)

        viewModel = QuoteViewModel(
            quoteStateHolder,
            savedStateHandle,
            categoryManager,
            toastManager,
            quoteLoadingManager,
            shareQuoteManager
        )
    }

    @Test
    fun `카테고리 변경시 상태가 초기화되고 새로운 데이터를 로드한다`() = mainCoroutineRule.runTest {
        // Given
        val category = QuoteCategory.EFFORT
        coEvery { quoteLoadingManager.fetchQuotesByCategory(any(), any(), any()) } returns
                QuoteResult(emptyList(), null)

        // When
        viewModel.onCategorySelected(category)

        // Then
        coVerify {
            quoteStateHolder.updateIsQuoteLoading(false)
            quoteStateHolder.updateHasQuoteReachedEnd(false)
            quoteStateHolder.updateSelectedCategory(category)
            quoteStateHolder.clearQuotes()
        }
    }

    @Test
    fun `네트워크 에러 발생시 적절한 토스트 메시지를 표시한다`() = mainCoroutineRule.runTest {
        // Given
        val category = QuoteCategory.EFFORT
        coEvery { quoteLoadingManager.fetchQuotesByCategory(any(), any(), any()) } throws
                IOException()

        // When
        viewModel.loadQuotes(category)

        // Then
        coVerify { toastManager.showNetworkErrorToast() }
    }
}