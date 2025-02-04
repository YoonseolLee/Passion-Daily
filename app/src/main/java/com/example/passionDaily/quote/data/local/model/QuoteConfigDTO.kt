package com.example.passionDaily.quote.data.local.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteConfigDTO(
    val day: Int,
    val category: String,
    val quoteId: String
)