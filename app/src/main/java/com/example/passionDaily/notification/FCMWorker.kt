package com.example.passionDaily.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import okhttp3.RequestBody.Companion.toRequestBody

@HiltWorker
class FCMWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val fcmService: FCMNotificationService,
    private val db: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/passion-daily-d8b51/messages:send"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val now = LocalTime.now()


            val currentHour = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

            // 현재 시간에 알림을 받을 사용자 조회
            val users = db.collection("users")
                .whereEqualTo("notificationEnabled", true)
                .whereEqualTo("notificationTime", currentHour)
                .get()
                .await()

            // 오늘의 명언 가져오기
            val todayQuote = fcmService.getQuoteForDay(dayOfWeek)

            if (todayQuote != null) {
                val message = FCMMessage(
                    body = "${todayQuote.text} - ${todayQuote.person}"
                )

                // 각 사용자에게 알림 전송
                users.documents.forEach { user ->
                    user.getString("fcmToken")?.let { token ->
                        sendNotification(token, message)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("FCMWorker", "Failed to send notifications", e)
            Result.failure()
        }
    }

    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                val asset = applicationContext.assets.open("service-account.json")
                GoogleCredentials.fromStream(asset)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                    .refreshAccessToken()
                    .tokenValue
            } catch (e: Exception) {
                Log.e("FCMWorker", "Error getting access token", e)
                throw e
            }
        }
    }

    private suspend fun sendNotification(token: String, message: FCMMessage) {
        try {
            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", message.title)
                        put("body", message.body)
                    })
                })
            }

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(FCM_URL)
                .addHeader("Authorization", "Bearer ${getAccessToken()}")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("FCMWorker", "FCM send failed: ${response.body?.string()}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FCMWorker", "Error sending FCM message", e)
        }
    }
}


