package com.example.passionDaily.notification.manager

import android.content.Context
import com.example.passionDaily.R
import com.example.passionDaily.notification.model.QuoteNotificationMessageDTO
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.example.passionDaily.resources.StringProvider
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
    @ApplicationContext private val context: Context,
    private val stringProvider: StringProvider,
) : FCMNotificationManager {

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        managerScope.launch {
            fcmService.monthlyQuotes.collect { quotes ->
            }
        }
    }

    override suspend fun sendQuoteNotification(
        quote: DailyQuoteDTO,
        users: List<DocumentSnapshot>
    ) {
        users.forEachIndexed { index, user ->
                user.getString(stringProvider.getString(R.string.fcmToken))?.let { token ->
                    val message = createNotificationMessage(quote)
                    sendNotification(token, message)
                }
        }
    }

    private fun createNotificationMessage(quote: DailyQuoteDTO): QuoteNotificationMessageDTO {
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
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getAccessToken(): String {
        return try {
            val credentials = loadCredentials()
            credentials.refreshAccessToken().tokenValue
        } catch (e: Exception) {
            throw e
        }
    }

    private fun loadCredentials(): GoogleCredentials {
        val asset = context.assets.open(stringProvider.getString(R.string.service_account_json))
        return GoogleCredentials.fromStream(asset)
            .createScoped(listOf(stringProvider.getString(R.string.firebase_messaging_url)))
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
            null
        }
    }

    private fun getQuoteForToday(): Triple<String, String, Int>? {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        return fcmService.monthlyQuotes.value.find { it.third == today }
            ?: run {
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
            throw e
        }
    }

    private fun buildFcmRequest(json: JSONObject, accessToken: String): Request {
        return Request.Builder()
            .url(stringProvider.getString(R.string.FCM_URL))
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun handleFcmResponse(response: Response) {
        if (!response.isSuccessful) {
            throw IOException("FCM send failed: ${response.code}")
        }
    }

    override fun cleanup() {
        managerScope.cancel()
    }
}
