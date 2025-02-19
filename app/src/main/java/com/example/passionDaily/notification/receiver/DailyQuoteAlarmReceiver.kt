package com.example.passionDaily.notification.receiver

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
import com.example.passionDaily.constants.ManagerConstants.DailyQuoteAlarmReceive.ALARM_REQUEST_CODE
import com.example.passionDaily.notification.worker.QuoteNotificationWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyQuoteAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        enqueueNotificationWork(context)
        scheduleNextAlarm(context)
        pendingResult.finish()
    }

    private fun enqueueNotificationWork(context: Context) {
        try {
            val workRequest = createNotificationWorkRequest()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            throw e
        }
    }

    fun createNotificationWorkRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<QuoteNotificationWorker>().build()
    }

    private fun scheduleNextAlarm(context: Context) {
        try {
            val alarmManager = getAlarmManager(context)
            val pendingIntent = createAlarmPendingIntent(context)
            val nextAlarmTime = getNextAlarmTime()

            setExactAlarm(alarmManager, nextAlarmTime, pendingIntent)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getAlarmManager(context: Context): AlarmManager {
        return try {
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        } catch (e: ClassCastException) {
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
            throw e
        }
    }

    fun getNextAlarmTime(): Long {
        return try {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
        } catch (e: Exception) {
            throw e
        }
    }

    private fun setExactAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
