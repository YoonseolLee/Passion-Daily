package com.example.passionDaily.notification

import android.app.AlarmManager
import android.app.PendingIntent
import javax.inject.Singleton
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AlarmScheduler"
        const val ALARM_REQUEST_CODE = 100
    }

    fun cancelExistingAlarm() {
        try {
            Log.d(TAG, "Attempting to cancel existing alarm")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Successfully cancelled existing alarm")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm", e)
        }
    }

    fun scheduleNotification(hour: Int, minute: Int) {
        try {
            Log.d(TAG, "Attempting to schedule notification for $hour:$minute")

            // 기존 알람 취소
            cancelExistingAlarm()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    Log.d(TAG, "Time already passed, scheduling for next day")
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            Log.d(TAG, "Setting alarm for: ${calendar.time}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Successfully scheduled alarm")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification", e)
        }
    }
}
