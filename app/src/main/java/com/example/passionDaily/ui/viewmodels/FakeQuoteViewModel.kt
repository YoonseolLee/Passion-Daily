package com.example.passionDaily.ui.viewmodels

import android.content.Context

class FakeQuoteViewModel : QuoteViewModelInterface {
    private val fakeCategories = listOf("노력", "운동", "자신감", "행복")

    override fun getQuoteCategories(): List<String> {
        return fakeCategories
    }

    override fun shareText(context: Context, text: String) {
        // Fake 클래스에서는 실제 공유 기능 대신 로그를 출력하거나 아무 작업도 하지 않습니다.
        println("Fake sharing text: $text")
    }
}