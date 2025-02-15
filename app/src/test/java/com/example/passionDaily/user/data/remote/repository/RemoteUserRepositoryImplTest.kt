package com.example.passionDaily.user.data.remote.repository

import com.example.passionDaily.constants.RepositoryConstants.RemoteUser.FAVORITES_COLLECTION
import com.example.passionDaily.constants.RepositoryConstants.RemoteUser.USERS_COLLECTION
import com.example.passionDaily.user.data.local.entity.UserEntity
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
        repository = RemoteUserRepositoryImpl(firestore, localUserRepository, timeUtil)
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
    fun `Firestore 사용자 정보를 Room으로 동기화한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val user = mockk<User>()
        val userEntity = mockk<UserEntity>()

        every {
            firestore.collection("users")
                .document(userId)
        } returns documentReference
        every { documentReference.get() } returns mockTask(documentSnapshot)
        every { documentSnapshot.toObject(User::class.java) } returns user
        every { localUserRepository.convertToUserEntity(user) } returns userEntity
        coEvery { localUserRepository.saveUser(userEntity) } just Runs

        // When
        repository.syncFirestoreUserToRoom(userId)

        // Then
        coVerify {
            documentReference.get()
            documentSnapshot.toObject(User::class.java)
            localUserRepository.convertToUserEntity(user)
            localUserRepository.saveUser(userEntity)
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
    fun `알림 설정을 Firestore에 업데이트한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val enabled = true

        every {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
        } returns documentReference
        every {
            documentReference.update("notificationEnabled", enabled)
        } returns mockTask(null)

        // When
        repository.updateNotificationSettingsToFirestore(userId, enabled)

        // Then
        verify {
            documentReference.update("notificationEnabled", enabled)
        }
    }

    @Test
    fun `알림 시간을 Firestore에 업데이트한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val newTime = "09:00"

        every {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
        } returns documentReference
        every {
            documentReference.update("notificationTime", newTime)
        } returns mockTask(null)

        // When
        repository.updateNotificationTimeToFirestore(userId, newTime)

        // Then
        verify {
            documentReference.update("notificationTime", newTime)
        }
    }

    @Test
    fun `Firestore에서 사용자 데이터를 삭제한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val usersDocRef = mockk<DocumentReference>(relaxed = true)
        val favoritesDocRef = mockk<DocumentReference>(relaxed = true)

        every {
            firestore.collection(USERS_COLLECTION).document(userId)
        } returns usersDocRef
        every {
            firestore.collection(FAVORITES_COLLECTION).document(userId)
        } returns favoritesDocRef
        every { usersDocRef.delete() } returns mockTask(null)
        every { favoritesDocRef.delete() } returns mockTask(null)

        // When
        repository.deleteUserDataFromFirestore(userId)

        // Then
        verify {
            usersDocRef.delete()
            favoritesDocRef.delete()
        }
    }
}