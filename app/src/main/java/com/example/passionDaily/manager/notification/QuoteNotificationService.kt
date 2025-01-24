package com.example.passionDaily.manager.notification

import android.util.Log
import com.example.passionDaily.data.constants.QuoteConfig
import com.example.passionDaily.data.constants.WeeklyQuoteData
import com.example.passionDaily.data.model.DailyQuote
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteNotificationService @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val db: FirebaseFirestore
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        val defaultQuotes = """
            [
                {"dayOfWeek":0,"category":"business","quoteId":"quote_000001"},
                {"dayOfWeek":1,"category":"confidence","quoteId":"quote_000001"},
                {"dayOfWeek":2,"category":"creativity","quoteId":"quote_000003"},
                {"dayOfWeek":3,"category":"creativity","quoteId":"quote_000014"},
                {"dayOfWeek":4,"category":"exercise","quoteId":"quote_000008"},
                {"dayOfWeek":5,"category":"other","quoteId":"quote_000001"},
                {"dayOfWeek":6,"category":"love","quoteId":"quote_000009"}
            ]
        """.trimIndent()
        remoteConfig.setDefaultsAsync(mapOf("weekly_quotes" to defaultQuotes))
    }

    private fun getWeeklyQuotes(): List<Triple<String, String, Int>> {
        try {
            val quotesJson = remoteConfig.getString("weekly_quotes")
            val quoteConfigs = json.decodeFromString<List<QuoteConfig>>(quotesJson)

            // QuoteConfig를 Triple로 변환
            return quoteConfigs.map { config ->
                Triple(config.category, config.quoteId, config.dayOfWeek)
            }
        } catch (e: Exception) {
            Log.e("FCMService", "Error parsing remote config, using default quotes", e)
            // 파싱 실패 시 기본 리스트 반환
            return listOf(
                Triple("business", "quote_000001", 0),
                Triple("confidence", "quote_000001", 1),
                Triple("creativity", "quote_000003", 2),
                Triple("creativity", "quote_000014", 3),
                Triple("exercise", "quote_000008", 4),
                Triple("other", "quote_000001", 5),
                Triple("love", "quote_000009", 6)
            )
        }
    }

    suspend fun getQuoteForDay(dayOfWeek: Int): DailyQuote? {
        return try {
            logDayOfWeek(dayOfWeek)
            val quoteInfo = findQuoteInfo(dayOfWeek) ?: return null
            logQuoteInfo(quoteInfo)
            fetchQuoteFromFirestore(quoteInfo)
        } catch (e: Exception) {
            logError(dayOfWeek, e)
            null
        }
    }

    private fun logDayOfWeek(dayOfWeek: Int) {
        Log.d("FCMService", "Getting quote for day: $dayOfWeek")
    }

    private fun findQuoteInfo(dayOfWeek: Int): Triple<String, String, Int>? {
        return WeeklyQuoteData.weeklyQuotes.find { it.third == dayOfWeek }
    }

    private fun logQuoteInfo(quoteInfo: Triple<String, String, Int>) {
        val (category, quoteId, _) = quoteInfo
        Log.d("FCMService", "Found category: $category, quoteId: $quoteId")
    }

    private suspend fun fetchQuoteFromFirestore(quoteInfo: Triple<String, String, Int>): DailyQuote {
        val (category, quoteId, _) = quoteInfo
        val quoteDoc = getQuoteDocument(category, quoteId)
        return createDailyQuote(quoteDoc).also {
            Log.d("FCMService", "Retrieved quote: $it")
        }
    }

    private suspend fun getQuoteDocument(category: String, quoteId: String): DocumentSnapshot {
        return db.collection("categories")
            .document(category)
            .collection("quotes")
            .document(quoteId)
            .get()
            .await()
    }

    private fun createDailyQuote(document: DocumentSnapshot): DailyQuote {
        return DailyQuote(
            text = document.getString("text") ?: "",
            person = document.getString("person") ?: ""
        )
    }

    private fun logError(dayOfWeek: Int, e: Exception) {
        Log.e("FCMService", "Error fetching quote for day $dayOfWeek", e)
    }
}