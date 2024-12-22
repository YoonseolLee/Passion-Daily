package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SharedQuoteViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel(), QuoteViewModelInterface{
    private val quoteCategories = QuoteCategory.values().map { it.koreanName }

    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(null)
    val selectedQuoteCategory: StateFlow<QuoteCategory?> = _selectedQuoteCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _currentQuoteIndex = MutableStateFlow(0)
    override val currentQuote: StateFlow<Quote?> = combine(_quotes, _currentQuoteIndex) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Pagination parameters
    private var isLoading = false
    private var lastLoadedQuote: DocumentSnapshot? = null
    private val pageSize = 20

    override fun nextQuote() {
        _currentQuoteIndex.update { currentIndex ->
            val nextIndex = currentIndex + 1
            if (nextIndex >= (_quotes.value.size - 5)) {
                // Load more quotes when approaching the end
                _selectedQuoteCategory.value?.let { loadMoreQuotes(it) }
            }
            if (nextIndex < _quotes.value.size) nextIndex else currentIndex
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            if (currentIndex > 0) currentIndex - 1 else currentIndex
        }
    }

    private fun loadMoreQuotes(category: QuoteCategory) {
        if (isLoading) return
        isLoading = true

        firestore.collection("categories")
            .document(category.koreanName)
            .collection("quotes")
            .orderBy("createdAt")
            .let { query ->
                lastLoadedQuote?.let { query.startAfter(it) } ?: query
            }
            .limit(pageSize.toLong())
            .get()
            .addOnSuccessListener { result ->
                val newQuotes = result.map { document ->
                    Quote(
                        id = document.id,
                        category = document.getString("category") ?: "",
                        text = document.getString("text") ?: "",
                        person = document.getString("person") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: 0L,
                        isDeleted = document.getBoolean("isDeleted") ?: false,
                        shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                    )
                }
                lastLoadedQuote = result.documents.lastOrNull()
                _quotes.update { currentQuotes -> currentQuotes + newQuotes }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching quotes: ${exception.message}")
                isLoading = false
            }
    }

    override fun getQuoteCategories(): List<String> {
        return quoteCategories
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
            Log.i("FetchQuotes", "selectedCategory : ${selectedCategory}")

            val db = Firebase.firestore
            db.collection("categories")
                .document(selectedCategory.koreanName)
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
                            shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                        )
                    }
                    Log.i("FetchQuotes", "quotes : ${quotes}")
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