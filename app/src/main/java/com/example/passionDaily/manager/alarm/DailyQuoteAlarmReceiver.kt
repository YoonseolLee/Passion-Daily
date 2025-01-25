package com.example.passionDaily.manager.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.passionDaily.manager.worker.QuoteNotificationWorker
import java.util.Calendar
import java.util.Date

class DailyQuoteAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DailyQuoteAlarmReceiver"
        private const val ALARM_REQUEST_CODE = 100
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        try {
            enqueueNotificationWork(context)
            scheduleNextAlarm(context)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "WorkManager is not initialized or invalid state: ${e.message}", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission issue while setting the alarm: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in AlarmReceiver: ${e.message}", e)
        } finally {
            pendingResult.finish()
        }
    }

    private fun enqueueNotificationWork(context: Context) {
        try {
            val workRequest = createNotificationWorkRequest()
            WorkManager.getInstance(context)
                .enqueue(workRequest)
                .also {
                    Log.d(TAG, "Notification work request enqueued")
                }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "WorkManager not properly initialized: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error enqueuing notification work: ${e.message}", e)
            throw e
        }
    }

    private fun createNotificationWorkRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<QuoteNotificationWorker>().build()
    }

    private fun scheduleNextAlarm(context: Context) {
        try {
            val alarmManager = getAlarmManager(context)
            val pendingIntent = createAlarmPendingIntent(context)
            val nextAlarmTime = getNextAlarmTime()

            setExactAlarm(alarmManager, nextAlarmTime, pendingIntent)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission issue while creating alarm: ${e.message}", e)
            throw e
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid argument while scheduling alarm: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while scheduling next alarm: ${e.message}", e)
            throw e
        }
    }

    private fun getAlarmManager(context: Context): AlarmManager {
        return try {
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        } catch (e: ClassCastException) {
            Log.e(TAG, "Failed to retrieve AlarmManager: ${e.message}", e)
            throw e
        }
    }

    private fun createAlarmPendingIntent(context: Context): PendingIntent {
        return try {
            val intent = Intent(context, DailyQuoteAlarmReceiver::class.java)
            PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission issue while creating pending intent: ${e.message}", e)
            throw e
        }
    }

    private fun getNextAlarmTime(): Long {
        return try {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating next alarm time: ${e.message}", e)
            throw e
        }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Next alarm scheduled for: ${Date(triggerAtMillis)}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission issue while setting exact alarm: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error setting exact alarm: ${e.message}", e)
            throw e
        }
    }
}
