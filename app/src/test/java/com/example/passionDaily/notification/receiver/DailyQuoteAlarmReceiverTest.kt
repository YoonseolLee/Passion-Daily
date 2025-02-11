package com.example.passionDaily.notification.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.passionDaily.notification.worker.QuoteNotificationWorker
import com.example.passionDaily.constants.ManagerConstants.DailyQuoteAlarmReceive.ALARM_REQUEST_CODE
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import androidx.work.OneTimeWorkRequest
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat

@RunWith(JUnit4::class)
class DailyQuoteAlarmReceiverTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `알림 작업이 큐에 추가되고 알람이 예약되는지 확인`() = mainCoroutineRule.runTest {
        // Given
        val context = mockk<Context>()
        val alarmManager = mockk<AlarmManager>(relaxed = true)
        val workManager = mockk<WorkManager>(relaxed = true)
        val receiver = DailyQuoteAlarmReceiver()

        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { WorkManager.getInstance(context) } returns workManager

        val pendingIntent = mockk<PendingIntent>(relaxed = true)
        every {
            PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                any<Intent>(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } returns pendingIntent

        // When
        receiver.onReceive(context, mockk())

        // Then
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
        verify {
            alarmManager.setExactAndAllowWhileIdle(any(), any(), any())
        }
    }

    @Test
    fun `알림 작업 실패 시 예외 처리 확인`() = mainCoroutineRule.runTest {
        // Given
        val context = mockk<Context>()
        val workManager = mockk<WorkManager>(relaxed = true)
        val receiver = DailyQuoteAlarmReceiver()

        every { WorkManager.getInstance(context) } returns workManager
        every { workManager.enqueue(any<OneTimeWorkRequest>()) } throws IllegalStateException("WorkManager initialization failed")

        // When
        receiver.onReceive(context, mockk())

        // Then
        verify(exactly = 1) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `알람 설정 시 보안 예외 처리 확인`() = mainCoroutineRule.runTest {
        // Given
        val context = mockk<Context>()
        val alarmManager = mockk<AlarmManager>(relaxed = true)
        val receiver = DailyQuoteAlarmReceiver()

        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { alarmManager.setExactAndAllowWhileIdle(any(), any(), any()) } throws SecurityException("Permission issue")

        // When
        receiver.onReceive(context, mockk())

        // Then
        verify(exactly = 1) { alarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
    }

    @Test
    fun `알림 예약을 위한 workRequest 생성`() = mainCoroutineRule.runTest {
        // Given
        val receiver = DailyQuoteAlarmReceiver()

        // When
        val workRequest = receiver.createNotificationWorkRequest()

        // Then
        assertThat(workRequest).isInstanceOf(OneTimeWorkRequest::class.java)
        assertThat(workRequest.workSpec.workerClassName).isEqualTo(QuoteNotificationWorker::class.java.name)
    }

    @Test
    fun `다음 알람 시간이 올바르게 계산되는지 확인`() = mainCoroutineRule.runTest {
        // Given
        val receiver = DailyQuoteAlarmReceiver()

        // When
        val nextAlarmTime = receiver.getNextAlarmTime()

        // Then
        assertThat(nextAlarmTime).isGreaterThan(System.currentTimeMillis())
    }
}