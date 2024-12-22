package com.example.passionDaily.ui.viewmodels

import android.content.Context
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class FakeQuoteViewModel : QuoteViewModelInterface {
    private val fakeCategories = listOf("노력", "운동", "자신감", "행복")
    var shareCountMap: MutableMap<String, Int> = mutableMapOf()



    // 가짜 데이터를 위한 quotes 리스트
    private val fakeQuotes = listOf(
        Quote(
            id = "1",
            category = "노력",
            text = "노력은 배신하지 않는다",
            person = "오타니",
            imageUrl = "",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            isDeleted = false,
            shareCount = 0
        ),
        Quote(
            id = "2",
            category = "노력",
            text = "포기하지 말고 한 걸음씩",
            person = "gunna",
            imageUrl = "",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            isDeleted = false,
            shareCount = 0
        ),
    )

    private val _currentQuoteIndex = MutableStateFlow(0)
    private val _quotes = MutableStateFlow(fakeQuotes)

    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(null)
    override val selectedQuoteCategory: StateFlow<QuoteCategory?> = _selectedQuoteCategory


    override val currentQuote: StateFlow<Quote?> =
        combine(_quotes, _currentQuoteIndex) { quotes, index ->
            quotes.getOrNull(index)
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Lazily,
            initialValue = null
        )

    override fun getQuoteCategories(): List<String> {
        return fakeCategories
    }

    override fun shareText(context: Context, text: String) {
        println("Fake sharing text: $text")
    }

    override fun nextQuote() {
        _currentQuoteIndex.update { currentIndex ->
            if (currentIndex < fakeQuotes.size - 1) currentIndex + 1 else currentIndex
        }
    }

    override fun previousQuote() {
        _currentQuoteIndex.update { currentIndex ->
            if (currentIndex > 0) currentIndex - 1 else currentIndex
        }
    }

    override fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        val currentCount = shareCountMap[quoteId] ?: 0
        shareCountMap[quoteId] = currentCount + 1
    }

    // 선택된 카테고리 설정하는 함수
    fun setSelectedQuoteCategory(category: QuoteCategory?) {
        _selectedQuoteCategory.value = category
    }
}
