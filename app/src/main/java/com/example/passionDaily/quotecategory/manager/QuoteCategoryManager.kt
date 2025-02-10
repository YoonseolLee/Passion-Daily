package com.example.passionDaily.quotecategory.manager

import com.example.passionDaily.quotecategory.model.QuoteCategory

interface QuoteCategoryManager {
    suspend fun setupCategory(category: String): QuoteCategory?
}