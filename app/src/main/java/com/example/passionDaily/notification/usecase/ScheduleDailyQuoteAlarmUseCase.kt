package com.example.passionDaily.notification.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import javax.inject.Singleton
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.passionDaily.constants.UseCaseConstants.ScheduleDailyQuoteAlarm.ALARM_REQUEST_CODE
import com.example.passionDaily.notification.receiver.DailyQuoteAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getAlarmManager().setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                getPendingIntent()
            )
        }
    }
}
