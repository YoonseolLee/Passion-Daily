package com.example.passionDaily.ui.viewmodels

class FakeCategoryViewModel : CategoryViewModelInterface {
    override fun getCategories(): List<String> {
        return listOf("노력", "부", "비즈니스", "사랑", "운동", "자신감", "창의력", "행복", "기타")
    }
}