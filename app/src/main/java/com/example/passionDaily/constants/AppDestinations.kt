package com.example.passionDaily.constants

object AppDestinations {
    const val QUOTE_ROUTE = "quote"
    const val CATEGORY_ROUTE = "category"
    const val FAVORITES_ROUTE = "favorites"
    const val SETTINGS_ROUTE = "settings"

    // 파라미터가 있는 라우트를 위한 함수
    fun quoteWithParams(category: String, quoteId: String) = "quote/$category/$quoteId"

    // 딥 링크 URI 패턴
    const val QUOTE_DEEP_LINK = "passiondaily://quote/{category}/{quoteId}"
}
