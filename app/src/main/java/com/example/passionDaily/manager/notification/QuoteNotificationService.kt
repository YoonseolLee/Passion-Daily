package com.example.passionDaily.manager.notification

import android.util.Log
import com.example.passionDaily.data.model.DailyQuote
import com.example.passionDaily.data.constants.WeeklyQuoteData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteNotificationService @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val weeklyQuotes = listOf(
        Triple("business", "quote_000001", 0),
        Triple("confidence", "quote_000001", 1),
        Triple("creativity", "quote_000003", 2),
        Triple("creativity", "quote_000014", 3),
        Triple("exercise", "quote_000008", 4),
        Triple("other", "quote_000001", 5),
        Triple("love", "quote_000009", 6)
    )

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