package com.example.passionDaily.notification

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FCMNotificationService @Inject constructor(
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

    suspend fun getQuoteForDay(dayOfWeek: Int): WeeklyQuote? {
        return try {
            Log.d("FCMService", "Getting quote for day: $dayOfWeek")
            val (category, quoteId, _) = WeeklyQuoteData.weeklyQuotes.find { it.third == dayOfWeek } ?: return null
            Log.d("FCMService", "Found category: $category, quoteId: $quoteId")

            val quoteDoc = db.collection("categories")
                .document(category)
                .collection("quotes")
                .document(quoteId)
                .get()
                .await()

            val quote = WeeklyQuote(
                text = quoteDoc.getString("text") ?: "",
                person = quoteDoc.getString("person") ?: ""
            )
            Log.d("FCMService", "Retrieved quote: $quote")
            quote
        } catch (e: Exception) {
            Log.e("FCMService", "Error fetching quote for day $dayOfWeek", e)
            null
        }
    }
}