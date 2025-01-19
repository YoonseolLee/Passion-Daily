package com.example.passionDaily.ui.components

import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.remote.model.Quote

interface QuoteDisplay {
    val id: String
    val text: String
    val person: String
    val imageUrl: String
}

// Quote 확장
fun Quote.toQuoteDisplay(): QuoteDisplay = object : QuoteDisplay {
    override val id: String = this@toQuoteDisplay.id
    override val text: String = this@toQuoteDisplay.text
    override val person: String = this@toQuoteDisplay.person
    override val imageUrl: String = this@toQuoteDisplay.imageUrl
}

// QuoteEntity 확장
fun QuoteEntity.toQuoteDisplay(): QuoteDisplay = object : QuoteDisplay {
    override val id: String = this@toQuoteDisplay.quoteId
    override val text: String = this@toQuoteDisplay.text
    override val person: String = this@toQuoteDisplay.person
    override val imageUrl: String = this@toQuoteDisplay.imageUrl
}