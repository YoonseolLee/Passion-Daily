package com.example.passionDaily.notification.manager

import android.content.Context
import android.util.Log
import com.example.passionDaily.constants.ManagerConstants.FCMNotification.FCM_URL
import com.example.passionDaily.constants.ManagerConstants.FCMNotification.TAG
import com.example.passionDaily.notification.model.QuoteNotificationMessageDTO
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMNotificationManagerImpl @Inject constructor(
    private val fcmService: QuoteNotificationService,
    @ApplicationContext private val context: Context
) : FCMNotificationManager {

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        managerScope.launch {
            fcmService.monthlyQuotes.collect { quotes ->
                Log.d(TAG, "Monthly quotes updated: ${quotes.size} quotes available")
            }
        }
    }

    override suspend fun sendQuoteNotification(
        quote: DailyQuoteDTO,
        users: List<DocumentSnapshot>
    ) {
        Log.d(TAG, "Starting to send notifications to ${users.size} users")
        users.forEachIndexed { index, user ->
            try {
                user.getString("fcmToken")?.let { token ->
                    Log.d(TAG, "Sending notification to user ${index + 1}/${users.size}")
                    val message = createNotificationMessage(quote)
                    sendNotification(token, message)
                } ?: Log.w(TAG, "User ${user.id} has no FCM token")
            } catch (e: IOException) {
                Log.e(TAG, "Network error sending notification to user ${user.id}: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending notification to user ${user.id}: ${e.message}")
            }
        }
    }

    private fun createNotificationMessage(quote: DailyQuoteDTO): QuoteNotificationMessageDTO {
        Log.d(
            TAG,
            "Creating notification with quote: text='${quote.text}', person='${quote.person}'"
        )
        return QuoteNotificationMessageDTO(
            body = "${quote.text} - ${quote.person}"
        )
    }

    private suspend fun sendNotification(token: String, message: QuoteNotificationMessageDTO) {
        try {
            val accessToken = getAccessToken()
            val json = createNotificationJson(token, message)
            if (json != null) {
                sendFcmRequest(json, accessToken)
                Log.d(TAG, "Successfully sent FCM request")
            } else {
                Log.e(TAG, "Failed to create notification JSON")
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Service account file not found: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "Network error in sendNotification: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendNotification: ${e.message}")
            throw e
        }
    }

    private fun getAccessToken(): String {
        return try {
            val credentials = loadCredentials()
            credentials.refreshAccessToken().tokenValue
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Service account file not found: ${e.message}")
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Network error getting access token: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting access token: ${e.message}")
            throw e
        }
    }

    private fun loadCredentials(): GoogleCredentials {
        val asset = context.assets.open("service-account.json")
        return GoogleCredentials.fromStream(asset)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    }

    private fun createNotificationJson(
        token: String,
        message: QuoteNotificationMessageDTO
    ): JSONObject? {
        return try {
            val quoteInfo = getQuoteForToday() ?: return null
            val (category, quoteId, _) = quoteInfo
            buildNotificationJson(token, message, category, quoteId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification JSON: ${e.message}")
            null
        }
    }

    private fun getQuoteForToday(): Triple<String, String, Int>? {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        return fcmService.monthlyQuotes.value.find { it.third == today }
            ?: run {
                Log.e(TAG, "No quote found for day $today")
                null
            }
    }

    private fun buildNotificationJson(
        token: String,
        message: QuoteNotificationMessageDTO,
        category: String,
        quoteId: String
    ): JSONObject {
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
                    put("priority", "HIGH")
                    put("notification", JSONObject().apply {
                        put("channel_id", "default")
                        put("visibility", "PUBLIC")
                        put("icon", "notification_icon")
                        put("vibrate_timings", JSONArray().apply {
                            put("1s")
                            put("1s")
                            put("1s")
                        })
                        put("default_vibrate_timings", false)
                    })
                })
            })
        }
    }

    private suspend fun sendFcmRequest(json: JSONObject, accessToken: String) {
        val client = OkHttpClient()
        val request = buildFcmRequest(json, accessToken)
        try {
            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    handleFcmResponse(response)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending FCM request: ${e.message}")
            throw e
        }
    }

    private fun buildFcmRequest(json: JSONObject, accessToken: String): Request {
        return Request.Builder()
            .url(FCM_URL)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun handleFcmResponse(response: Response) {
        val responseBody = response.body?.string()
        if (!response.isSuccessful) {
            Log.e(TAG, "FCM send failed: $responseBody")
            throw IOException("FCM send failed: ${response.code}")
        } else {
            Log.d(TAG, "FCM message sent successfully. Response: $responseBody")
        }
    }

    override fun cleanup() {
        managerScope.cancel()
    }
}
