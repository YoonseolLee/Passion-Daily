package com.example.passionDaily.ui.viewmodels

import android.content.Context
import com.example.passionDaily.data.remote.model.Quote
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

    // 가짜 데이터를 위한 quotes 리스트
    private val fakeQuotes = listOf(
        Quote(
            id = "1",
            category = "노력",
            text = "노력은 배신하지 않는다",
            person = "박명수",
            imageUrl = "",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            isDeleted = false,
            viewCount = 0,
            shareCount = 0
        ),
        Quote(
            id = "2",
            category = "운동",
            text = "포기하지 말고 한 걸음씩",
            person = "유재석",
            imageUrl = "",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            isDeleted = false,
            viewCount = 0,
            shareCount = 0
        ),
        // 더 많은 가짜 데이터를 추가할 수 있습니다
    )

    private val _currentQuoteIndex = MutableStateFlow(0)
    private val _quotes = MutableStateFlow(fakeQuotes)

    override val currentQuote: StateFlow<Quote?> = combine(_quotes, _currentQuoteIndex) { quotes, index ->
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
        // Fake 클래스에서는 실제 공유 기능 대신 로그를 출력
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
}