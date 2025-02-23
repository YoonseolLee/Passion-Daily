package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GetRequiredDataUseCaseTest {

    private val getRequiredDataUseCase = GetRequiredDataUseCase()

    @Test
    fun `모든 값이 정상일 경우 Pair를 반환한다`() {
        // Given
        val selectedCategory = mockk<QuoteCategory>()
        val quotes = listOf(mockk<Quote> {
            every { id } returns "quote1"
        })
        val quoteId = "quote1"

        // When & Then
        val result = getRequiredDataUseCase.getRequiredDataForAdd(
            selectedCategory,
            quotes,
            quoteId
        )
        assertNotNull(result)
    }

    @Test
    fun `QuoteCategory가 null일 경우 IllegalStateException을 던진다`() {
        // Given
        val currentUser = mockk<FirebaseUser>()
        val selectedCategory: QuoteCategory? = null
        val quotes = listOf(mockk<Quote> { every { id } returns "quote1" })
        val quoteId = "quote1"

        // When & Then
        val exception = assertThrows(IllegalStateException::class.java) {
            getRequiredDataUseCase.getRequiredDataForAdd(
                selectedCategory,
                quotes,
                quoteId
            )
        }
        assertEquals("No category selected", exception.message)
    }

    @Test
    fun `Quote가 없을 경우 IllegalStateException을 던진다`() {
        // Given
        val currentUser = mockk<FirebaseUser>()
        val selectedCategory = mockk<QuoteCategory>()
        val quotes = listOf(mockk<Quote> { every { id } returns "quote2" })
        val quoteId = "quote1"

        // When & Then
        val exception = assertThrows(IllegalStateException::class.java) {
            getRequiredDataUseCase.getRequiredDataForAdd(
                selectedCategory,
                quotes,
                quoteId
            )
        }
        assertEquals("Quote not found: quote1", exception.message)
    }
}
