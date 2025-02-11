package com.example.passionDaily.quote.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.passionDaily.database.PassionDailyDatabase
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class QuoteDaoInjectionTest {
    private lateinit var database: PassionDailyDatabase
    private lateinit var quoteDao: QuoteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassionDailyDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        quoteDao = database.quoteDao()
    }

    @Test
    fun testSqlInjectionGetQuoteById() = runBlocking {
        // 정상 데이터 삽입
        val legitimateQuote = QuoteEntity(
            quoteId = "1",
            text = "정상적인 인용구",
            person = "홍길동",
            imageUrl = "https://example.com/image.jpg",
            categoryId = 1
        )
        quoteDao.insertQuote(legitimateQuote)

        // SQL Injection 시도 패턴들
        val maliciousInputs = listOf(
            "1' OR '1'='1",
            "1'; DELETE FROM quotes; --",
            "1' UNION SELECT * FROM quotes--",
            "1' OR 'x'='x",
            "1'; DROP TABLE quotes; --"
        )

        maliciousInputs.forEach { maliciousInput ->
            val result = quoteDao.getQuoteById(maliciousInput)

            if (result != null) {
                assertEquals(maliciousInput, result.quoteId)
            }

            // 원본 데이터가 보존되었는지 확인
            val originalQuote = quoteDao.getQuoteById("1")
            assertNotNull(originalQuote)
            assertEquals("정상적인 인용구", originalQuote?.text)
        }
    }

    @Test
    fun testSqlInjectionInList() = runBlocking {
        // 정상 데이터 삽입
        val quote1 = QuoteEntity(
            quoteId = "1",
            text = "인용구 1",
            person = "홍길동",
            imageUrl = "https://example.com/1.jpg",
            categoryId = 1
        )
        val quote2 = QuoteEntity(
            quoteId = "2",
            text = "인용구 2",
            person = "김철수",
            imageUrl = "https://example.com/2.jpg",
            categoryId = 1
        )
        quoteDao.insertQuote(quote1)
        quoteDao.insertQuote(quote2)

        val maliciousIds = listOf(
            "1",
            "2' OR '1'='1",
            "'); DELETE FROM quotes; --"
        )

        val results = quoteDao.getQuotesByIds(maliciousIds)

        assertTrue(results.size <= 2)
        results.forEach { quote ->
            assertTrue(quote.quoteId in listOf("1", "2"))
        }
    }

    @Test
    fun testSqlInjectionInCategoryId() = runBlocking {
        val quote = QuoteEntity(
            quoteId = "1",
            text = "테스트 인용구",
            person = "홍길동",
            imageUrl = "https://example.com/test.jpg",
            categoryId = 1
        )
        quoteDao.insertQuote(quote)

        val maliciousCategoryId = "1' OR '1'='1"

        quoteDao.getQuotesByCategory(maliciousCategoryId).test {
            val results = awaitItem()
            assertTrue(results.isEmpty() || results.all { it.categoryId.toString() == maliciousCategoryId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testSqlInjectionInDelete() = runBlocking {
        val quote = QuoteEntity(
            quoteId = "1",
            text = "삭제 테스트용 인용구",
            person = "홍길동",
            imageUrl = "https://example.com/delete.jpg",
            categoryId = 1
        )
        quoteDao.insertQuote(quote)

        val maliciousQuoteId = "1' OR '1'='1"
        val maliciousCategoryId = 1

        quoteDao.deleteQuote(maliciousQuoteId, maliciousCategoryId)

        // 검증: 원래 데이터가 여전히 존재하는지 확인
        val originalQuote = quoteDao.getQuoteById("1")
        assertNotNull(originalQuote)
    }

    @Test
    fun testSqlInjectionInQuoteWithCategory() = runBlocking {
        val quote = QuoteEntity(
            quoteId = "1",
            text = "카테고리 테스트 인용구",
            person = "홍길동",
            imageUrl = "https://example.com/category.jpg",
            categoryId = 1
        )
        quoteDao.insertQuote(quote)

        val maliciousQuoteId = "1' OR '1'='1"

        val result = quoteDao.getQuoteWithCategory(maliciousQuoteId)

        if (result != null) {
            assertEquals(maliciousQuoteId, result.quote.quoteId)
        }
    }

    @After
    fun cleanup() {
        database.close()
    }
}