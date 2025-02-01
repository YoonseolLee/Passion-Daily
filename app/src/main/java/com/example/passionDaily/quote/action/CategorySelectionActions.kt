package com.example.passionDaily.quote.action

import com.example.passionDaily.util.QuoteCategory

interface CategorySelectionActions {
    fun onCategorySelected(category: QuoteCategory?)
}