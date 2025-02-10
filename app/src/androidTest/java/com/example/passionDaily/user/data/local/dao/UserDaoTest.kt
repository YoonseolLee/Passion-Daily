package com.example.passionDaily.user.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.database.PassionDailyDatabase
import com.example.passionDaily.user.data.local.entity.UserEntity
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: PassionDailyDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassionDailyDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetUserById() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )

        // When
        userDao.insertUser(user)

        // Then
        val retrieved = userDao.getUserByUserId("user1")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.email).isEqualTo("test@example.com")
    }

    @Test
    fun updateUser() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        // When
        val updated = user.copy(email = "updated@example.com")
        userDao.updateUser(updated)

        // Then
        val retrieved = userDao.getUserByUserId("user1")
        assertThat(retrieved?.email).isEqualTo("updated@example.com")
    }

    @Test
    fun deleteUser() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        // When
        userDao.deleteUser("user1")

        // Then
        val retrieved = userDao.getUserByUserId("user1")
        assertThat(retrieved).isNull()
    }

    @Test
    fun getAllUsers() = mainCoroutineRule.runTest {
        // Given
        val users = listOf(
            UserEntity("user1", "test1@example.com", true, "09:00", System.currentTimeMillis()),
            UserEntity("user2", "test2@example.com", false, "10:00", System.currentTimeMillis())
        )
        users.forEach { userDao.insertUser(it) }

        // When
        val allUsers = userDao.getAllUsers()

        // Then
        assertThat(allUsers).hasSize(2)
        assertThat(allUsers.map { it.userId }).containsExactly("user1", "user2")
    }

    @Test
    fun getUserByEmail() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        // When
        val retrieved = userDao.getUserByEmail("test@example.com")

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.userId).isEqualTo("user1")
    }

    @Test
    fun getUsersWithNotifications() = mainCoroutineRule.runTest {
        // Given
        val users = listOf(
            UserEntity("user1", "test1@example.com", true, "09:00", System.currentTimeMillis()),
            UserEntity("user2", "test2@example.com", false, "10:00", System.currentTimeMillis()),
            UserEntity("user3", "test3@example.com", true, "11:00", System.currentTimeMillis())
        )
        users.forEach { userDao.insertUser(it) }

        // When
        val enabledUsers = userDao.getUsersWithNotifications(true)
        val disabledUsers = userDao.getUsersWithNotifications(false)

        // Then
        assertThat(enabledUsers).hasSize(2)
        assertThat(disabledUsers).hasSize(1)
        assertThat(enabledUsers.map { it.userId }).containsExactly("user1", "user3")
    }

    @Test
    fun deleteUserById() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        // When
        userDao.deleteUserById("user1")

        // Then
        val retrieved = userDao.getUserByUserId("user1")
        assertThat(retrieved).isNull()
    }

    @Test
    fun updateNotificationSetting() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        // When
        userDao.updateNotificationSetting("user1", false)

        // Then
        val retrieved = userDao.getUserByUserId("user1")
        assertThat(retrieved?.notificationEnabled).isFalse()
    }

    @Test
    fun updateNotificationTime() = mainCoroutineRule.runTest {
        // Given
        val user = UserEntity(
            userId = "user1",
            email = "test@example.com",
            notificationEnabled = true,
            notificationTime = "09:00",
            lastSyncDate = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        // When
        userDao.updateNotificationTime("user1", "10:00")

        // Then
        val retrieved = userDao.getUserByUserId("user1")
        assertThat(retrieved?.notificationTime).isEqualTo("10:00")
    }
}