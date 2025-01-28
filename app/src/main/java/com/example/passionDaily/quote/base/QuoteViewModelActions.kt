package com.example.passionDaily.quote.base

import android.content.Context
import com.example.passionDaily.util.QuoteCategory

interface QuoteViewModelActions {
    fun previousQuote()
    fun nextQuote()
    fun navigateToQuoteWithCategory(quoteId: String, category: String)
    fun shareQuote(context: Context, imageUrl: String?, quoteText: String, author: String)
    fun incrementShareCount(quoteId: String, category: QuoteCategory?)
    fun loadInitialQuotes(category: QuoteCategory?)
    fun onCategorySelected(category: QuoteCategory?)
    fun loadQuotes(category: QuoteCategory)
}