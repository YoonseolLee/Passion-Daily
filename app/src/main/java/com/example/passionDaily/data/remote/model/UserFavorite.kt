package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class UserFavorite(
    val id: String = "", // quote_id
    val quoteRef: DocumentReference? = null,
    val createdDate: Timestamp = Timestamp.now(),
    val quote: QuoteInfo = QuoteInfo()
) {
    data class QuoteInfo(
        val text: String = "",
        val author: String = "",
        val category: String = ""
    )
}