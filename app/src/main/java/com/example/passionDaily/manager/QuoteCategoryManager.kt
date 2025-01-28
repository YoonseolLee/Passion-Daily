package com.example.passionDaily.manager

import android.util.Log
import com.example.passionDaily.constants.ViewModelConstants.Quote.TAG
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.util.QuoteCategory
import javax.inject.Inject

class QuoteCategoryManager @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder
) {

    suspend fun setupCategory(category: String): QuoteCategory? {
        val quoteCategory = findCategory(category) ?: return null

        updateQuoteCategory(quoteCategory)

        return quoteCategory
    }

    private fun findCategory(category: String): QuoteCategory? {
        return QuoteCategory.values()
            .find { it.name.lowercase() == category.lowercase() }
            .also {
                if (it == null) Log.w(TAG, "Invalid category: $category")
            }
    }

    private suspend fun updateQuoteCategory(category: QuoteCategory) {
        quoteStateHolder.updateSelectedCategory(category)
    }
}