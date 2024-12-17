package com.example.passionDaily.util


enum class Categories {
    EFFORT, WEALTH, BUSINESS, LOVE, EXERCISE, CONFIDENCE, CREATIVITY, HAPPINESS, OTHER;

    fun toKorean(): String {
        return when (this) {
            EFFORT -> "노력"
            WEALTH -> "부"
            BUSINESS -> "비즈니스"
            LOVE -> "사랑"
            EXERCISE -> "운동"
            CONFIDENCE -> "자신감"
            CREATIVITY -> "창의력"
            HAPPINESS -> "행복"
            OTHER -> "기타"
        }
    }
}
