package com.example.passionDaily.util

enum class QuoteCategory(val koreanName: String) {
    EFFORT("노력"),
    WEALTH("부"),
    BUSINESS("비즈니스"),
    LOVE("사랑"),
    EXERCISE("운동"),
    CONFIDENCE("자신감"),
    CREATIVITY("창의력"),
    HAPPINESS("행복"),
    OTHER("기타");

    companion object {
        fun fromKoreanName(koreanName: String): QuoteCategory? {
            return entries.find { it.koreanName == koreanName }
        }
    }

    // toString()을 오버라이드하여 koreanName을 반환하도록 함
    override fun toString(): String {
        return koreanName
    }
}
