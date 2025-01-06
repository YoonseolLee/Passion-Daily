package com.example.passionDaily.data.remote.model

import com.example.passionDaily.util.QuoteCategory

data class Quote(
    val id: String,
    val category: QuoteCategory,
    val text: String,
    val person: String,
    val imageUrl: String,
    val createdAt: String,
    val modifiedAt: String,
    val shareCount: Int
)
