package com.example.passionDaily.favorites.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.database.PassionDailyDatabase
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import app.cash.turbine.test
import com.example.passionDaily.favorites.data.local.dto.FavoriteWithCategory
import com.example.passionDaily.quote.data.local.dao.QuoteDao
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quotecategory.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.user.data.local.dao.UserDao
import com.example.passionDaily.user.data.local.entity.UserEntity
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: PassionDailyDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var userDao: UserDao
    private lateinit var quoteDao: QuoteDao
    private lateinit var categoryDao: QuoteCategoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassionDailyDatabase::class.java
        ).allowMainThreadQueries().build()

        favoriteDao = database.favoriteDao()
        userDao = database.userDao()
        quoteDao = database.quoteDao()
        categoryDao = database.categoryDao()

        runBlocking {
            categoryDao.insertCategory(QuoteCategoryEntity(
                categoryId = 1,
                categoryName = "Test Category 1"
            ))
            categoryDao.insertCategory(QuoteCategoryEntity(
                categoryId = 2,
                categoryName = "Test Category 2"
            ))

            userDao.insertUser(UserEntity(
                userId = "user1",
                email = "test@example.com",
                notificationEnabled = true,
                notificationTime = "09:00",
                lastSyncDate = System.currentTimeMillis()
            ))

            quoteDao.insertQuote(QuoteEntity(
                quoteId = "quote1",
                text = "Test Quote 1",
                person = "Author 1",
                imageUrl = "https://example.com/image1.jpg",
                categoryId = 1
            ))
            quoteDao.insertQuote(QuoteEntity(
                quoteId = "quote2",
                text = "Test Quote 2",
                person = "Author 2",
                imageUrl = "https://example.com/image2.jpg",
                categoryId = 2
            ))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertFavorite_ThenCheckIfExists() = mainCoroutineRule.runTest {
        // Given
        val favorite = FavoriteEntity(userId = "user1", quoteId = "quote1", categoryId = 1)

        // When
        favoriteDao.insertFavorite(favorite)

        // Then
        favoriteDao.checkFavoriteEntity("user1", "quote1", 1)
            .test {
                val result = awaitItem()
                assertThat(result).isNotNull()
                assertThat(result?.quoteId).isEqualTo("quote1")
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun insertFavorite_ThenRetrieveAllFavorites() = mainCoroutineRule.runTest {
        // Given
        val favorite = FavoriteEntity(userId = "user1", quoteId = "quote1", categoryId = 1)
        favoriteDao.insertFavorite(favorite)

        // When & Then
        favoriteDao.getAllFavorites("user1")
            .test {
                val favorites = awaitItem()
                assertThat(favorites).isNotEmpty()
                assertThat(favorites.first().quoteId).isEqualTo("quote1")
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun deleteFavorite_ThenCheckIfDeleted() = mainCoroutineRule.runTest  {
        // Given
        val favorite = FavoriteEntity(userId = "user1", quoteId = "quote1", categoryId = 1)
        favoriteDao.insertFavorite(favorite)

        // When
        favoriteDao.deleteFavorite("user1", "quote1", 1)

        // Then
        favoriteDao.checkFavoriteEntity("user1", "quote1", 1)
            .test {
                val result = awaitItem()
                assertThat(result).isNull()
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun deleteAllFavoritesByUserId_ThenCheckIfEmpty() = mainCoroutineRule.runTest  {
        // Given
        val favorite1 = FavoriteEntity(userId = "user1", quoteId = "quote1", categoryId = 1)
        val favorite2 = FavoriteEntity(userId = "user1", quoteId = "quote2", categoryId = 2)
        favoriteDao.insertFavorite(favorite1)
        favoriteDao.insertFavorite(favorite2)

        // When
        favoriteDao.deleteAllFavoritesByUserId("user1")

        // Then
        favoriteDao.getAllFavorites("user1")
            .test {
                val favorites = awaitItem()
                assertThat(favorites).isEmpty()
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun getAllFavoriteIds_ReturnsCorrectIds() = mainCoroutineRule.runTest  {
        // Given
        val favorite1 = FavoriteEntity(userId = "user1", quoteId = "quote1", categoryId = 1)
        val favorite2 = FavoriteEntity(userId = "user1", quoteId = "quote2", categoryId = 2)
        favoriteDao.insertFavorite(favorite1)
        favoriteDao.insertFavorite(favorite2)

        // When & Then
        favoriteDao.getAllFavoriteIds("user1")
            .test {
                val ids = awaitItem()
                assertThat(ids).containsExactly("quote1", "quote2")
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun getAllFavoriteIdsWithCategory_ReturnsCorrectData() = mainCoroutineRule.runTest  {
        // Given
        val favorite1 = FavoriteEntity(userId = "user1", quoteId = "quote1", categoryId = 1)
        val favorite2 = FavoriteEntity(userId = "user1", quoteId = "quote2", categoryId = 2)
        favoriteDao.insertFavorite(favorite1)
        favoriteDao.insertFavorite(favorite2)

        // When & Then
        favoriteDao.getAllFavoriteIdsWithCategory("user1")
            .test {
                val favorites = awaitItem()
                assertThat(favorites).hasSize(2)
                assertThat(favorites).containsExactly(
                    FavoriteWithCategory("quote1", 1),
                    FavoriteWithCategory("quote2", 2)
                )
                cancelAndIgnoreRemainingEvents()
            }
    }
}