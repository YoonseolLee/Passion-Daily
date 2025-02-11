package com.example.passionDaily.settings.manager

import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.settings.domain.usecase.ParseTimeUseCase
import com.example.passionDaily.settings.domain.usecase.SaveNotificationUseCase
import com.example.passionDaily.settings.domain.usecase.UpdateNotificationUseCase
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.base.Verify.verify
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

@ExperimentalCoroutinesApi
class NotificationManagerImplTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val updateNotificationUseCase: UpdateNotificationUseCase = mockk(relaxed = true)
    private val scheduleAlarmUseCase: ScheduleDailyQuoteAlarmUseCase = mockk(relaxed = true)
    private val parseTimeUseCase: ParseTimeUseCase = mockk(relaxed = true)
    private val saveNotificationUseCase: SaveNotificationUseCase = mockk(relaxed = true)

    private val notificationManager = NotificationManagerImpl(
        updateNotificationUseCase,
        scheduleAlarmUseCase,
        parseTimeUseCase,
        saveNotificationUseCase
    )

    @Test
    fun `업데이트 알림 설정 호출 테스트`() = mainCoroutineRule.runTest {
        // given
        val userId = "user1"
        val enabled = true

        // when
        notificationManager.updateNotificationSettings(userId, enabled)

        // then
        coVerify { updateNotificationUseCase.updateNotificationSettings(userId, enabled) }
    }

    @Test
    fun `스케줄 알람 호출 테스트`() {
        // given
        val hour = 10
        val minute = 30

        // when
        notificationManager.scheduleNotification(hour, minute)

        // then
        verify { scheduleAlarmUseCase.scheduleNotification(hour, minute) }
    }

    @Test
    fun `기존 알람 취소 호출 테스트`() {
        // given

        // when
        notificationManager.cancelExistingAlarm()

        // then
        verify { scheduleAlarmUseCase.cancelExistingAlarm() }
    }

    @Test
    fun `시간 문자열 파싱 테스트`() {
        // given
        val timeStr = "10:30"
        val expectedTime = LocalTime.of(10, 30)
        every { parseTimeUseCase.parseTime(timeStr) } returns expectedTime

        // when
        val actualTime = notificationManager.parseTime(timeStr)

        // then
        assertThat(actualTime).isEqualTo(expectedTime)
    }

    @Test
    fun `알림 시간 업데이트 호출 테스트`() = mainCoroutineRule.runTest {
        // given
        val userId = "user1"
        val time = LocalTime.of(9, 0)

        // when
        notificationManager.updateNotificationTime(userId, time)

        // then
        coVerify { saveNotificationUseCase.updateNotificationTime(userId, time) }
    }
}