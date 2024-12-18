package com.example.passionDaily.ui.viewmodels

import android.content.Context

interface QuoteViewModelInterface {
    fun getCategories(): List<String>
    fun shareText(context: Context, text: String)
}