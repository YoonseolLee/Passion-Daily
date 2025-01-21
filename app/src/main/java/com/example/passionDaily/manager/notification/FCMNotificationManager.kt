package com.example.passionDaily.manager.notification

import android.content.Context
import android.util.Log
import com.example.passionDaily.data.constants.WeeklyQuoteData
import com.example.passionDaily.data.model.DailyQuote
import com.example.passionDaily.data.model.QuoteNotificationMessage
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMNotificationManager @Inject constructor(
    private val fcmService: QuoteNotificationService,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FCMNotificationManager"
        private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/passion-daily-d8b51/messages:send"
    }

    suspend fun sendQuoteNotification(quote: DailyQuote, users: List<DocumentSnapshot>) {
        Log.d(TAG, "Starting to send notifications to ${users.size} users")
        users.forEachIndexed { index, user ->
            try {
                user.getString("fcmToken")?.let { token ->
                    Log.d(TAG, "Sending notification to user ${index + 1}/${users.size}")
                    val message = createNotificationMessage(quote)
                    sendNotification(token, message)
                } ?: Log.w(TAG, "User ${user.id} has no FCM token")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending notification to user ${user.id}", e)
            }
        }
    }

    private fun createNotificationMessage(quote: DailyQuote): QuoteNotificationMessage {
        return QuoteNotificationMessage(
            body = "${quote.text} - ${quote.person}"
        )
    }

    private suspend fun sendNotification(token: String, message: QuoteNotificationMessage) {
        try {
            Log.d(TAG, "Getting access token")
            val accessToken = getAccessToken()

            Log.d(TAG, "Creating notification JSON")
            val json = createNotificationJson(token, message)

            Log.d(TAG, "Sending FCM request")
            sendFcmRequest(json, accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendNotification", e)
            throw e
        }
    }

    private suspend fun getAccessToken(): String {
        return try {
            Log.d(TAG, "Opening service account file")
            val asset = context.assets.open("service-account.json")

            Log.d(TAG, "Creating credentials")
            val credentials = GoogleCredentials.fromStream(asset)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            Log.d(TAG, "Refreshing access token")
            credentials.refreshAccessToken().tokenValue
        } catch (e: Exception) {
            Log.e(TAG, "Error getting access token", e)
            throw e
        }
    }

    private fun createNotificationJson(token: String, message: QuoteNotificationMessage): JSONObject {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        val (category, quoteId, _) = WeeklyQuoteData.weeklyQuotes.find { it.third == dayOfWeek }
            ?: return JSONObject()

        return JSONObject().apply {
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
    }

    private suspend fun sendFcmRequest(json: JSONObject, accessToken: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(FCM_URL)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "FCM send failed: $responseBody")
                    throw IOException("FCM send failed: ${response.code}")
                } else {
                    Log.d(TAG, "FCM message sent successfully. Response: $responseBody")
                }
            }
        }
    }
}