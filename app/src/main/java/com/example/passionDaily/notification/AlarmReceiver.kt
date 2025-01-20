package com.example.passionDaily.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    private val fcmService: FCMNotificationService by lazy {
        FCMNotificationService(FirebaseFirestore.getInstance())
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "AlarmReceiver triggered")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
                Log.d("AlarmReceiver", "Current time: $currentTime, Day of week: $dayOfWeek")

                // 오늘의 명언 가져오기
                val todayQuote = fcmService.getQuoteForDay(dayOfWeek)
                Log.d("AlarmReceiver", "Today's quote: $todayQuote")

                if (todayQuote != null) {
                    val message = FCMMessage(
                        body = "${todayQuote.text} - ${todayQuote.person}"
                    )

                    // 현재 시간에 알림을 받을 사용자 조회
                    val users = FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("notificationEnabled", true)
                        .whereEqualTo("notificationTime", currentTime)
                        .get()
                        .await()

                    Log.d("AlarmReceiver", "Found ${users.size()} users for notification")
                    users.documents.forEach { user ->
                        Log.d("AlarmReceiver", "Processing user: ${user.id}")
                        user.getString("fcmToken")?.let { token ->
                            Log.d("AlarmReceiver", "Sending notification to token: $token")
                            sendNotification(context, token, message)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error in AlarmReceiver", e)
            } finally {
                scheduleNextAlarm(context)
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
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
            Log.d("AlarmReceiver", "Next alarm scheduled for: ${calendar.time}")
        }
    }

    private suspend fun sendNotification(context: Context, token: String, message: FCMMessage) {
        try {
            val inputStream = context.assets.open("service-account.json")
            Log.d("FCM", "Successfully opened service-account.json")

            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            val accessToken = withContext(Dispatchers.IO) {
                credentials.refreshAccessToken().tokenValue
            }
            Log.d("FCM", "Got access token: ${accessToken.take(10)}...")

            // WeeklyQuoteData에서 현재 요일에 해당하는 카테고리와 quoteId 가져오기
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            val (category, quoteId, _) = WeeklyQuoteData.weeklyQuotes.find { it.third == dayOfWeek }
                ?: return

            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", message.title)
                        put("body", message.body)
                    })
                    put("data", JSONObject().apply {
                        put("category", category)
                        put("quoteId", quoteId)
                    })
                    put("android", JSONObject().apply {
                        put("priority", "high")
                    })
                })
            }
            Log.d("FCM", "Sending message: $json")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/passion-daily-d8b51/messages:send")
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        Log.e("FCM", "FCM send failed: $responseBody")
                        throw IOException("FCM send failed: ${response.code}")
                    } else {
                        Log.d("FCM", "FCM message sent successfully. Response: $responseBody")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error sending FCM message", e)
            throw e
        }
    }
}
