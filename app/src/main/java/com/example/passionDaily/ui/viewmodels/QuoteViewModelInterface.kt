package com.example.passionDaily.ui.viewmodels

import android.content.Context

interface QuoteViewModelInterface {
    fun getQuoteCategories(): List<String>
    fun shareText(context: Context, text: String)
}