package com.example.passionDaily.notification.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import javax.inject.Singleton
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.passionDaily.constants.UseCaseConstants.ScheduleDailyQuoteAlarm.ALARM_REQUEST_CODE
import com.example.passionDaily.notification.receiver.DailyQuoteAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@Singleton
class ScheduleDailyQuoteAlarmUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun cancelExistingAlarm() {
        executeWithExceptionHandling {
            getPendingIntent()?.let { pendingIntent ->
                getAlarmManager().cancel(pendingIntent)
            }
        }
    }

    fun scheduleNotification(hour: Int, minute: Int) {
        executeWithExceptionHandling {
            cancelExistingAlarm()
            schedulePreciseAlarm(createAlarmCalendar(hour, minute))
        }
    }

    private fun executeWithExceptionHandling(block: () -> Unit) {
            block()
    }

    private fun getAlarmManager(): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun getAlarmIntent(): Intent {
        return Intent(context, DailyQuoteAlarmReceiver::class.java)
    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            getAlarmIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmCalendar(hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    private fun schedulePreciseAlarm(calendar: Calendar) {
        val alarmManager = getAlarmManager()
        val pendingIntent = getPendingIntent()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // 정확한 알람 권한이 없을 경우 대체 방법 사용
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        10 * 60 * 1000, // 10분 이내 실행 허용
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // 권한 없음 - 비정확 알람으로 대체
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                10 * 60 * 1000, // 10분 이내 실행 허용
                pendingIntent
            )
        }
    }
}
