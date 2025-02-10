package com.example.passionDaily.notification.data.repository.remote

import com.example.passionDaily.util.MainCoroutineRule
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@ExperimentalCoroutinesApi
class UserNotificationRepositoryTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var db: FirebaseFirestore
    private lateinit var repository: UserNotificationRepository
    private lateinit var collectionReference: CollectionReference
    private lateinit var query: Query
    private lateinit var querySnapshot: QuerySnapshot

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
        db = mockk()
        collectionReference = mockk()
        query = mockk()
        querySnapshot = mockk()
        repository = spyk(UserNotificationRepository(db), recordPrivateCalls = true)

        every { db.collection("users") } returns collectionReference
        every { collectionReference.whereEqualTo("notificationEnabled", true) } returns query
    }

    @Test
    fun `알림을 받을 사용자 목록을 가져온다`() = mainCoroutineRule.runTest {
        // Given
        val currentTime = "09:00"
        every { query.whereEqualTo("notificationTime", currentTime) } returns query
        every { query.get() } returns mockTask(querySnapshot)

        // When
        val result = repository.getTargetUsers(currentTime)

        // Then
        assertThat(result).isEqualTo(querySnapshot)

        // 메소드 호출 검증
        verify(exactly = 1) {
            db.collection("users")
            collectionReference.whereEqualTo("notificationEnabled", true)
            query.whereEqualTo("notificationTime", currentTime)
            query.get()
        }
    }

    @Test
    fun `쿼리 실패시 예외가 발생한다`() = mainCoroutineRule.runTest {
        // Given
        val currentTime = "09:00"
        val exception = RuntimeException("Firestore query failed")

        every { query.whereEqualTo("notificationTime", currentTime) } returns query
        every { query.get() } returns mockTask(null, exception)

        // When & Then
        try {
            repository.getTargetUsers(currentTime)
            error("Expected exception was not thrown")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().contains("Firestore query failed")
        }

        // 메소드 호출 검증
        verify(exactly = 1) {
            db.collection("users")
            collectionReference.whereEqualTo("notificationEnabled", true)
            query.whereEqualTo("notificationTime", currentTime)
            query.get()
        }
    }
}