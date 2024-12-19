package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import com.example.passionDaily.util.Gender
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedQuoteViewModel @Inject constructor() : ViewModel(), QuoteViewModelInterface{
    private val quoteCategory = QuoteCategory.values().map { it.toKorean() }

    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(null)
    val selectedQuoteCategory: StateFlow<QuoteCategory?> = _selectedQuoteCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    init {
        fetchQuotes(_selectedQuoteCategory.value)
    }

    override fun getQuoteCategories(): List<String> {
        return quoteCategory
    }

    override fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 공유 타입 설정
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "공유하기")
        context.startActivity(chooser)
    }

    fun onCategorySelected(category: QuoteCategory?) {
        _selectedQuoteCategory.value = category
        fetchQuotes(category)
    }

    private fun fetchQuotes(selectedCategory: QuoteCategory?) {
        try {
            if (selectedCategory == null) {
                Log.e("FetchQuotes", "Category is null")
                return
            }

            val db = Firebase.firestore
            db.collection("categories")
                .document(selectedCategory.toKorean())
                .collection("quotes")
                .get()
                .addOnSuccessListener { result ->
                    val quotes = result.map { document ->
                        Quote(
                            id = document.id,
                            category = document.getString("category") ?: "",
                            text = document.getString("text") ?: "",
                            person = document.getString("person") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                            modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: 0L,
                            isDeleted = document.getBoolean("isDeleted") ?: false,
                            viewCount = document.getLong("viewCount")?.toInt() ?: 0,
                            shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                        )
                    }
                    _quotes.value = quotes
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching quotes: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("FetchQuotes", "Unexpected error: ${e.message}")
        }
    }
}