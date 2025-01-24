package com.example.passionDaily.data.constants

import kotlinx.serialization.Serializable

@Serializable
data class QuoteConfig(
    val dayOfWeek: Int,
    val category: String,
    val quoteId: String
)