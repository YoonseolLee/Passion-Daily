package com.example.passionDaily.notification.usecase

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Build
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class ScheduleDailyQuoteAlarmUseCaseTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var useCase: ScheduleDailyQuoteAlarmUseCase

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)
        pendingIntent = mockk(relaxed = true)

        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager

        mockkStatic(PendingIntent::class)
        every {
            PendingIntent.getBroadcast(
                any(),
                any(),
                any(),
                any()
            )
        } returns pendingIntent

        useCase = ScheduleDailyQuoteAlarmUseCase(context)
    }

    @Test
    fun `기존 알람 취소 기능이 정상적으로 동작하는지 확인`() = mainCoroutineRule.runTest{
        // given
        every { alarmManager.cancel(any<PendingIntent>()) } just Runs

        // when
        useCase.cancelExistingAlarm()

        // then
        verify { alarmManager.cancel(pendingIntent) }
    }

    @Test
    fun `알람이 정상적으로 예약되는지 확인`() = mainCoroutineRule.runTest{
        // given
        val hour = 10
        val minute = 30
        val expectedCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        every {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                any(),
                any()
            )
        } just Runs

        // when
        useCase.scheduleNotification(hour, minute)

        // then
        verify {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                match { timeMillis ->
                    val scheduledTime = Calendar.getInstance().apply {
                        timeInMillis = timeMillis
                    }
                    scheduledTime.get(Calendar.HOUR_OF_DAY) == hour &&
                            scheduledTime.get(Calendar.MINUTE) == minute &&
                            scheduledTime.get(Calendar.SECOND) == 0
                },
                pendingIntent
            )
        }
    }

    @Test
    fun `현재 시간이 지나면 알람이 다음날로 예약되는지 확인`()  = mainCoroutineRule.runTest{
        // given
        val now = Calendar.getInstance()
        val hourBeforeNow = (now.get(Calendar.HOUR_OF_DAY) - 1).coerceIn(0, 23)
        val minute = 0

        every {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                any(),
                any()
            )
        } just Runs

        // when
        useCase.scheduleNotification(hourBeforeNow, minute)

        // then
        verify {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                match { timeMillis ->
                    val scheduledTime = Calendar.getInstance().apply {
                        timeInMillis = timeMillis
                    }
                    val expectedTime = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, hourBeforeNow)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }
                    scheduledTime.get(Calendar.DAY_OF_MONTH) == expectedTime.get(Calendar.DAY_OF_MONTH) &&
                            scheduledTime.get(Calendar.HOUR_OF_DAY) == hourBeforeNow &&
                            scheduledTime.get(Calendar.MINUTE) == minute &&
                            scheduledTime.get(Calendar.SECOND) == 0
                },
                pendingIntent
            )
        }
    }

    @Test
    fun `알람 예약 시 PendingIntent 생성 시 올바른 플래그가 사용되는지 확인`() = mainCoroutineRule.runTest{
        // given & when
        useCase.scheduleNotification(10, 30)

        // then
        verify {
            PendingIntent.getBroadcast(
                any(),
                any(),
                any(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}