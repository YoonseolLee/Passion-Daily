package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.passionDaily.util.Categories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedQuoteViewModel @Inject constructor() : ViewModel(), QuoteViewModelInterface{
    private val categories = Categories.values().map { it.toKorean() }

    override fun getCategories(): List<String> {
        return categories
    }

    override fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 공유 타입 설정
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "공유하기")
        context.startActivity(chooser)
    }
}