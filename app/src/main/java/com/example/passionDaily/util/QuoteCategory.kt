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

        fun fromEnglishName(name: String): QuoteCategory? {
            return entries.find { it.name.lowercase() == name.lowercase() }
        }
    }

    fun getLowercaseCategoryId(): String {
        return name.lowercase()
    }

    override fun toString(): String {
        return koreanName
    }
}
