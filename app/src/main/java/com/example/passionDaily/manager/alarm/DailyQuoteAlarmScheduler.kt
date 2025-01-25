package com.example.passionDaily.manager.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import javax.inject.Singleton
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

@Singleton
class DailyQuoteAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "AlarmScheduler"
        const val ALARM_REQUEST_CODE = 100
    }

    fun cancelExistingAlarm() {
        try {
            getPendingIntent()?.let { pendingIntent ->
                getAlarmManager().cancel(pendingIntent)
                Log.d(TAG, "Successfully cancelled existing alarm")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while cancelling alarm", e)
        } catch (e: NullPointerException) {
            Log.e(TAG, "PendingIntent is null while cancelling alarm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while cancelling alarm", e)
        }
    }

    fun scheduleNotification(hour: Int, minute: Int) {
        try {
            cancelExistingAlarm()
            schedulePreciseAlarm(createAlarmCalendar(hour, minute))
            Log.d(TAG, "Successfully scheduled alarm")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid time arguments provided: hour=$hour, minute=$minute", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while scheduling notification", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while scheduling notification", e)
        }
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
            Log.d(TAG, "Next alarm scheduled for: ${calendar.time}")
        }
    }
}
