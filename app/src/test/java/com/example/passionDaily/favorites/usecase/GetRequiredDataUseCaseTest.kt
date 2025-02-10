package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertNotNull
import org.junit.Test

class GetRequiredDataUseCaseTest {

    private val getRequiredDataUseCase = GetRequiredDataUseCase()

    @Test
    fun `모든 값이 정상일 경우 Triple을 반환한다`() {
        // Given
        val currentUser = mockk<FirebaseUser>()
        val selectedCategory = mockk<QuoteCategory>()
        val quotes = listOf(mockk<Quote> {
            coEvery { id } returns "quote1"
        })
        val quoteId = "quote1"

        // When
        val result = getRequiredDataUseCase.getRequiredDataForAdd(
            currentUser,
            selectedCategory,
            quotes,
            quoteId
        )

        // Then
        assertNotNull(result)
    }

    @Test
    fun `FirebaseUser가 null일 경우 null을 반환한다`() {
        // Given
        val currentUser: FirebaseUser? = null
        val selectedCategory = mockk<QuoteCategory>()
        val quotes = listOf(mockk<Quote> { coEvery { id } returns "quote1" })
        val quoteId = "quote1"

        // When
        val result = getRequiredDataUseCase.getRequiredDataForAdd(
            currentUser,
            selectedCategory,
            quotes,
            quoteId
        )

        // Then
        assertNull(result)
    }

    @Test
    fun `QuoteCategory가 null일 경우 null을 반환한다`() {
        // Given
        val currentUser = mockk<FirebaseUser>()
        val selectedCategory: QuoteCategory? = null
        val quotes = listOf(mockk<Quote> { coEvery { id } returns "quote1" })
        val quoteId = "quote1"

        // When
        val result = getRequiredDataUseCase.getRequiredDataForAdd(
            currentUser,
            selectedCategory,
            quotes,
            quoteId
        )

        // Then
        assertNull(result)
    }

    @Test
    fun `Quote가 없을 경우 null을 반환한다`() {
        // Given
        val currentUser = mockk<FirebaseUser>()
        val selectedCategory = mockk<QuoteCategory>()
        val quotes = listOf(mockk<Quote> { coEvery { id } returns "quote2" })
        val quoteId = "quote1"

        // When
        val result = getRequiredDataUseCase.getRequiredDataForAdd(
            currentUser,
            selectedCategory,
            quotes,
            quoteId
        )

        // Then
        assertNull(result)
    }
}
