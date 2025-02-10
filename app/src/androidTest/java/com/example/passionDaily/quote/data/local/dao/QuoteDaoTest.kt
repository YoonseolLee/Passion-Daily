package com.example.passionDaily.quote.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.database.PassionDailyDatabase
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import app.cash.turbine.test
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quotecategory.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class QuoteDaoTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: PassionDailyDatabase
    private lateinit var quoteDao: QuoteDao
    private lateinit var categoryDao: QuoteCategoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassionDailyDatabase::class.java
        ).allowMainThreadQueries().build()

        quoteDao = database.quoteDao()
        categoryDao = database.categoryDao()

        // Insert required category first
        runBlocking {
            categoryDao.insertCategory(
                QuoteCategoryEntity(
                    categoryId = 1,
                    categoryName = "Test Category"
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetQuoteById() = mainCoroutineRule.runTest {
        // Given
        val quote = QuoteEntity(
            quoteId = "quote1",
            text = "Test Quote",
            person = "Test Person",
            imageUrl = "test.jpg",
            categoryId = 1
        )

        // When
        quoteDao.insertQuote(quote)

        // Then
        val retrieved = quoteDao.getQuoteById("quote1")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.text).isEqualTo("Test Quote")
    }

    @Test
    fun getQuotesByIds() = mainCoroutineRule.runTest {
        // Given
        val quotes = listOf(
            QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1),
            QuoteEntity("quote2", "Quote 2", "Person 2", "image2.jpg", 1)
        )
        quotes.forEach { quoteDao.insertQuote(it) }

        // When
        val retrieved = quoteDao.getQuotesByIds(listOf("quote1", "quote2"))

        // Then
        assertThat(retrieved).hasSize(2)
        assertThat(retrieved.map { it.quoteId }).containsExactly("quote1", "quote2")
    }

    @Test
    fun getQuotesByCategory() = mainCoroutineRule.runTest {
        // Given
        val quote = QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1)
        quoteDao.insertQuote(quote)

        // When & Then
        quoteDao.getQuotesByCategory("1").test {
            val quotes = awaitItem()
            assertThat(quotes).hasSize(1)
            assertThat(quotes.first().quoteId).isEqualTo("quote1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateQuote() = mainCoroutineRule.runTest {
        // Given
        val quote = QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1)
        quoteDao.insertQuote(quote)

        // When
        val updated = quote.copy(text = "Updated Quote")
        quoteDao.updateQuote(updated)

        // Then
        val retrieved = quoteDao.getQuoteById("quote1")
        assertThat(retrieved?.text).isEqualTo("Updated Quote")
    }

    @Test
    fun deleteQuote() = mainCoroutineRule.runTest {
        // Given
        val quote = QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1)
        quoteDao.insertQuote(quote)

        // When
        quoteDao.deleteQuote("quote1", 1)

        // Then
        val retrieved = quoteDao.getQuoteById("quote1")
        assertThat(retrieved).isNull()
    }

    @Test
    fun getQuoteWithCategory() = mainCoroutineRule.runTest {
        // Given
        val quote = QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1)
        quoteDao.insertQuote(quote)

        // When
        val quoteWithCategory = quoteDao.getQuoteWithCategory("quote1")

        // Then
        assertThat(quoteWithCategory).isNotNull()
        assertThat(quoteWithCategory?.quote?.quoteId).isEqualTo("quote1")
        assertThat(quoteWithCategory?.category?.categoryId).isEqualTo(1)
    }

    @Test
    fun deleteAllQuotes() = mainCoroutineRule.runTest {
        // Given
        val quotes = listOf(
            QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1),
            QuoteEntity("quote2", "Quote 2", "Person 2", "image2.jpg", 1)
        )
        quotes.forEach { quoteDao.insertQuote(it) }

        // When
        quoteDao.deleteAllQuotes()

        // Then
        val allQuotes = quoteDao.getQuotesByIds(listOf("quote1", "quote2"))
        assertThat(allQuotes).isEmpty()
    }

    @Test
    fun isQuoteExistsInCategory() = mainCoroutineRule.runTest {
        // Given
        val quote = QuoteEntity("quote1", "Quote 1", "Person 1", "image1.jpg", 1)
        quoteDao.insertQuote(quote)

        // When & Then
        val exists = quoteDao.isQuoteExistsInCategory("quote1", 1)
        assertThat(exists).isTrue()

        val notExists = quoteDao.isQuoteExistsInCategory("quote1", 2)
        assertThat(notExists).isFalse()
    }
}