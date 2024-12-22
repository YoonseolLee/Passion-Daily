package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp

data class FavoritesDocument(
    val addedAt: Timestamp = Timestamp.now(),
    val category: String = "",
    val quoteId: String = ""
)
