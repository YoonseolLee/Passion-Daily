package com.example.passionDaily.quote.action

import com.example.passionDaily.quotecategory.model.QuoteCategory

interface CategorySelectionActions {
    fun onCategorySelected(category: QuoteCategory?)
}