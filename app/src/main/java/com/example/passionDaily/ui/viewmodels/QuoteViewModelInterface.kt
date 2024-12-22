package com.example.passionDaily.ui.viewmodels

import android.content.Context
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface QuoteViewModelInterface {
    val currentQuote: StateFlow<Quote?>
    val selectedQuoteCategory: StateFlow<QuoteCategory?>

    fun getQuoteCategories(): List<String>
    fun shareText(context: Context, text: String)
    fun nextQuote()     // 추가
    fun previousQuote() // 추가
    fun incrementShareCount(quoteId: String, category: QuoteCategory?)
}