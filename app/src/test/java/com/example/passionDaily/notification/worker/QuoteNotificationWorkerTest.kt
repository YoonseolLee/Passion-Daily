package com.example.passionDaily.notification.worker

import android.content.Context
import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.example.passionDaily.notification.manager.FCMNotificationManager
import com.example.passionDaily.notification.data.repository.remote.UserNotificationRepository
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.example.passionDaily.util.MainCoroutineRule

@ExperimentalCoroutinesApi
class QuoteNotificationWorkerTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var fcmService: QuoteNotificationService
    private lateinit var fcmManager: FCMNotificationManager
    private lateinit var userRepository: UserNotificationRepository
    private lateinit var worker: QuoteNotificationWorker

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        fcmService = mockk()
        fcmManager = mockk()
        userRepository = mockk()

        worker = QuoteNotificationWorker(
            context = context,
            params = workerParams,
            fcmService = fcmService,
            fcmManager = fcmManager,
            userRepository = userRepository
        )
    }

    @Test
    fun `알림_발송_성공시_Success_반환`() = mainCoroutineRule.runTest {
        // given
        val mockQuote = mockk<DailyQuoteDTO>()
        val mockUsers = mockk<QuerySnapshot>()
        val mockDocuments = listOf<DocumentSnapshot>(mockk(), mockk())

        coEvery { fcmService.getQuoteForToday() } returns mockQuote
        coEvery { userRepository.getTargetUsers(any()) } returns mockUsers
        every { mockUsers.documents } returns mockDocuments
        coEvery { fcmManager.sendQuoteNotification(any(), any()) } just Runs

        // when
        val result = worker.doWork()

        // then
        assertThat(result).isEqualTo(Result.success())
        coVerify { fcmService.getQuoteForToday() }
        coVerify { userRepository.getTargetUsers(any()) }
        coVerify { fcmManager.sendQuoteNotification(mockQuote, mockDocuments) }
    }

    @Test
    fun `오늘의_명언_없을때_Success_반환`() = mainCoroutineRule.runTest {
        // given
        coEvery { fcmService.getQuoteForToday() } returns null

        // when
        val result = worker.doWork()

        // then
        assertThat(result).isEqualTo(Result.success())
        coVerify { fcmService.getQuoteForToday() }
        coVerify(exactly = 0) { userRepository.getTargetUsers(any()) }
        coVerify(exactly = 0) { fcmManager.sendQuoteNotification(any(), any()) }
    }

    @Test
    fun `예외_발생시_Failure_반환`() = mainCoroutineRule.runTest {
        // given
        coEvery { fcmService.getQuoteForToday() } throws Exception("Test exception")

        // when
        val result = worker.doWork()

        // then
        assertThat(result).isEqualTo(Result.failure())
        coVerify { fcmService.getQuoteForToday() }
        coVerify(exactly = 0) { userRepository.getTargetUsers(any()) }
        coVerify(exactly = 0) { fcmManager.sendQuoteNotification(any(), any()) }
    }
}
