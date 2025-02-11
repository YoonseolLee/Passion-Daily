package com.example.passionDaily.notification.manager

import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.google.firebase.firestore.DocumentSnapshot

interface FCMNotificationManager {
    suspend fun sendQuoteNotification(quote: DailyQuoteDTO, users: List<DocumentSnapshot>)
    fun cleanup()
}

