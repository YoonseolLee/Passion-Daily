package com.example.passionDaily.notification.data.repository.remote

import com.example.passionDaily.util.MainCoroutineRule
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class UserNotificationRepositoryTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var db: FirebaseFirestore
    private lateinit var repository: UserNotificationRepository
    private lateinit var collectionRef: CollectionReference
    private lateinit var query: Query
    private lateinit var queryTask: Task<QuerySnapshot>
    private lateinit var querySnapshot: QuerySnapshot

    @Before
    fun setup() {
        // 필요한 모든 Firebase 관련 객체들을 모의 객체로 생성
        db = mockk(relaxed = true)
        collectionRef = mockk(relaxed = true)
        query = mockk(relaxed = true)
        queryTask = mockk(relaxed = true)
        querySnapshot = mockk(relaxed = true)
        repository = UserNotificationRepository(db)

        // 기본적인 체이닝 동작 설정
        every { db.collection("users") } returns collectionRef
        every { collectionRef.whereEqualTo("notificationEnabled", true) } returns query
        every { query.whereEqualTo("notificationTime", any()) } returns query
    }

    @Test
    fun `특정 시간에 알림을 받을 사용자 목록을 가져온다`() = runTest {
        // Given
        val currentTime = "09:00"
        val latch = CountDownLatch(1)
        var result: QuerySnapshot? = null
        var testError: Exception? = null

        every { query.get() } returns queryTask
        every { queryTask.addOnSuccessListener(any()) } answers {
            firstArg<(QuerySnapshot) -> Unit>().invoke(querySnapshot)
            latch.countDown()
            queryTask
        }
        every { queryTask.addOnFailureListener(any()) } answers {
            queryTask
        }

        // When
        try {
            result = repository.getTargetUsers(currentTime)
        } catch (e: Exception) {
            testError = e
        }

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue() // 5초 타임아웃 설정
        assertThat(testError).isNull()
        assertThat(result).isEqualTo(querySnapshot)
    }

    @Test
    fun `Firestore 쿼리 실패시 예외가 발생한다`() = runTest {
        // Given
        val currentTime = "09:00"
        val latch = CountDownLatch(1)
        val testException = RuntimeException("Firestore query failed")
        var result: QuerySnapshot? = null
        var testError: Exception? = null

        every { query.get() } returns queryTask
        every { queryTask.addOnSuccessListener(any()) } answers { queryTask }
        every { queryTask.addOnFailureListener(any()) } answers {
            firstArg<(Exception) -> Unit>().invoke(testException)
            latch.countDown()
            queryTask
        }

        // When
        try {
            result = repository.getTargetUsers(currentTime)
        } catch (e: Exception) {
            testError = e
        }

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(testError).isNotNull()
        assertThat(testError).hasMessageThat().contains("Firestore query failed")
        assertThat(result).isNull()
    }
}