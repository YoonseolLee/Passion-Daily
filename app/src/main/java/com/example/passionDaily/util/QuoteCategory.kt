package com.example.passionDaily.util

enum class QuoteCategory(
    val koreanName: String,
    val categoryId: Int
) {
    EFFORT("노력",0),
    WEALTH("부",1),
    BUSINESS("비즈니스",2),
    LOVE("사랑",3),
    EXERCISE("운동",4),
    CONFIDENCE("자신감",5),
    CREATIVITY("창의력",6),
    HAPPINESS("행복",7),
    OTHER("기타",8);

    companion object {
        fun fromKoreanName(koreanName: String): QuoteCategory? {
            return entries.find { it.koreanName == koreanName }
        }

        fun fromEnglishName(name: String): QuoteCategory? {
            return entries.find { it.name.lowercase() == name.lowercase() }
        }

        fun fromCategoryId(categoryId: Int): QuoteCategory {
            return values().find { it.categoryId == categoryId }
                ?: throw IllegalArgumentException("Invalid category ID: $categoryId")
        }
    }

    fun getLowercaseCategoryId(): String {
        return name.lowercase()
    }

    override fun toString(): String {
        return koreanName
    }
}
