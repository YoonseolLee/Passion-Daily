package com.example.passionDaily.ui.viewmodels

import android.content.Context
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface QuoteViewModelInterface {
    val currentQuote: StateFlow<Quote?>
    val selectedQuoteCategory: StateFlow<QuoteCategory?>

    fun getQuoteCategories(): List<String>
    fun shareText(context: Context, text: String)
    fun nextQuote()
    fun previousQuote()
    fun incrementShareCount(quoteId: String, category: QuoteCategory?)
    fun addFavorite(quoteId: String)
    fun isFavorite(quoteId: String): Flow<Boolean>
    fun removeFavorite(quoteId: String)
    fun fetchFavoriteQuotes()
}