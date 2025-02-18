package com.example.passionDaily.notification.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import io.mockk.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.work.OneTimeWorkRequest
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class DailyQuoteAlarmReceiverTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var alarmManager: AlarmManager
    private lateinit var receiver: DailyQuoteAlarmReceiver

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)
        receiver = spyk(DailyQuoteAlarmReceiver())

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager

        every {
            context.getSystemService(Context.ALARM_SERVICE)
        } returns alarmManager
    }

    @Test
    fun `다음_알람시간_계산_검증`() {
        // given
        val currentTime = Calendar.getInstance()

        // when
        val nextAlarmTime = receiver.getNextAlarmTime()
        val nextAlarmCalendar = Calendar.getInstance().apply {
            timeInMillis = nextAlarmTime
        }

        // then
        assertThat(nextAlarmCalendar.get(Calendar.DAY_OF_MONTH))
            .isEqualTo(currentTime.get(Calendar.DAY_OF_MONTH) + 1)
    }

    @Test
    fun `알람_수신시_워크매니저_작업등록_및_다음알람_설정되어야_함`() = mainCoroutineRule.runTest {
        // given
        val intent = mockk<Intent>()
        val pendingResult = mockk<BroadcastReceiver.PendingResult>(relaxed = true)
        val workRequest = mockk<OneTimeWorkRequest>()
        every { receiver.goAsync() } returns pendingResult
        every { receiver.createNotificationWorkRequest() } returns workRequest

        // when
        receiver.onReceive(context, intent)

        // then
        verify {
            workManager.enqueue(workRequest)
            pendingResult.finish()
        }
    }
}