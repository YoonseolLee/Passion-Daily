package com.example.passionDaily.quotecategory.manager

import android.util.Log
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import javax.inject.Inject

class QuoteCategoryManagerImpl @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder
) : QuoteCategoryManager {

    companion object {
        private const val TAG = "QuoteCategoryManager"
    }

    override suspend fun setupCategory(category: String): QuoteCategory? {
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
