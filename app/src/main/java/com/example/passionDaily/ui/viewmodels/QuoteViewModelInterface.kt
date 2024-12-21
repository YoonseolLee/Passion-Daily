package com.example.passionDaily.ui.viewmodels

import android.content.Context
import com.example.passionDaily.data.remote.model.Quote
import kotlinx.coroutines.flow.StateFlow

interface QuoteViewModelInterface {
    val currentQuote: StateFlow<Quote?>  // 추가
    fun getQuoteCategories(): List<String>
    fun shareText(context: Context, text: String)
    fun nextQuote()     // 추가
    fun previousQuote() // 추가
}