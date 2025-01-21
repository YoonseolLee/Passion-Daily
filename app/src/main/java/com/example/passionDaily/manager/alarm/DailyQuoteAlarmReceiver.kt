package com.example.passionDaily.manager.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.passionDaily.data.model.QuoteNotificationMessage
import com.example.passionDaily.manager.notification.QuoteNotificationService
import com.example.passionDaily.data.constants.WeeklyQuoteData
import com.example.passionDaily.data.model.DailyQuote
import com.example.passionDaily.manager.worker.QuoteNotificationWorker
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class DailyQuoteAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        try {
            // WorkManager를 통해 알림 작업 예약
            val workRequest = OneTimeWorkRequestBuilder<QuoteNotificationWorker>()
                .build()

            WorkManager.getInstance(context)
                .enqueue(workRequest)
                .also {
                    Log.d(TAG, "Notification work request enqueued")
                }

            // 다음 날 알람 예약
            scheduleNextAlarm(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error in AlarmReceiver", e)
        } finally {
            pendingResult.finish()
        }
    }

    private fun scheduleNextAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyQuoteAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Next alarm scheduled for: ${calendar.time}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling next alarm", e)
        }
    }

    companion object {
        private const val TAG = "DailyQuoteAlarmReceiver"
        private const val ALARM_REQUEST_CODE = 100
    }
}
