package com.example.passionDaily.user.data.remote.repository

import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.model.User
import com.example.passionDaily.util.MainCoroutineRule
import com.example.passionDaily.util.TimeUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@ExperimentalCoroutinesApi
class RemoteUserRepositoryImplTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var localUserRepository: LocalUserRepository
    private lateinit var timeUtil: TimeUtil
    private lateinit var repository: RemoteUserRepositoryImpl
    private lateinit var documentReference: DocumentReference
    private lateinit var documentSnapshot: DocumentSnapshot
    private lateinit var stringProvider: StringProvider

    // mockTask 헬퍼 함수
    private inline fun <reified T> mockTask(result: T?, exception: Exception? = null): Task<T> {
        val task: Task<T> = mockk(relaxed = true)
        every { task.isComplete } returns true
        every { task.exception } returns exception
        every { task.isCanceled } returns false
        every { task.result } returns result
        return task
    }

    @Before
    fun setup() {
        firestore = mockk(relaxed = true)
        localUserRepository = mockk(relaxed = true)
        timeUtil = mockk(relaxed = true)
        documentReference = mockk(relaxed = true)
        documentSnapshot = mockk(relaxed = true)
        stringProvider = mockk(relaxed = true)
        repository = RemoteUserRepositoryImpl(firestore, localUserRepository, timeUtil, stringProvider)
    }

    @Test
    fun `사용자가 등록되어 있는지 확인한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every { documentReference.get() } returns mockTask(documentSnapshot)
        every { documentSnapshot.exists() } returns true

        // When
        val result = repository.isUserRegistered(userId)

        // Then
        assertThat(result).isTrue()
        verify {
            firestore.collection("users")
            documentReference.get()
            documentSnapshot.exists()
        }
    }

    @Test
    fun `마지막 동기화 날짜를 업데이트한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val currentTime = "2024-02-10T12:34:56Z"
        every { timeUtil.getCurrentTimestamp() } returns currentTime
        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every {
            documentReference.update(any<Map<String, Any>>())
        } returns mockTask(null)

        // When
        repository.updateLastSyncDate(userId)

        // Then
        verify {
            timeUtil.getCurrentTimestamp()
            documentReference.update(
                mapOf(
                    "lastSyncDate" to currentTime,
                    "lastLoginDate" to currentTime
                )
            )
        }
    }

    @Test
    fun `사용자 프로필을 추가한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val profileMap = mapOf(
            "name" to "Test User",
            "email" to "test@example.com"
        )
        val fcmTask: Task<String> = mockTask("test_token")
        val firebaseMessaging = mockk<FirebaseMessaging>()

        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns firebaseMessaging
        every { firebaseMessaging.token } returns fcmTask

        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every { documentReference.get() } returns mockTask(documentSnapshot)
        every { documentSnapshot.exists() } returns false
        every { documentReference.set(profileMap) } returns mockTask(null)
        every { documentReference.update("fcmToken", any()) } returns mockTask(null)

        // When
        repository.addUserProfile(userId, profileMap)

        // Then
        verify {
            documentReference.get()
            documentReference.set(profileMap)
            FirebaseMessaging.getInstance()
            firebaseMessaging.token
        }

        unmockkStatic(FirebaseMessaging::class)
    }

    @Test
    fun 알림_설정을_Firestore에_업데이트한다() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val enabled = true
        every { stringProvider.getString(any()) } returns "users"
        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every {
            documentReference.update("notificationEnabled", enabled)
        } returns mockTask(null)

        // When
        repository.updateNotificationSettingsToFirestore(userId, enabled)

        // Then
        verify {
            firestore.collection("users")
            documentReference.update("notificationEnabled", enabled)
        }
    }

    @Test
    fun 알림_시간을_Firestore에_업데이트한다() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val newTime = "09:00"
        every { stringProvider.getString(any()) } returns "users"
        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every {
            documentReference.update("notificationTime", newTime)
        } returns mockTask(null)

        // When
        repository.updateNotificationTimeToFirestore(userId, newTime)

        // Then
        verify {
            firestore.collection("users")
            documentReference.update("notificationTime", newTime)
        }
    }

    @Test
    fun Firestore에서_사용자_데이터를_삭제한다() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        every { stringProvider.getString(any()) } returns "users"
        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every { documentReference.delete() } returns mockTask(null)

        // When
        repository.deleteUserDataFromFirestore(userId)

        // Then
        verify {
            firestore.collection("users")
            documentReference.delete()
        }
    }
}