package com.example.passionDaily.quote.data.remote.model

import java.sql.Timestamp

// saved_quotes 컬렉션의 quote_id 문서를 나타내는 클래스
data class SavedQuote(
    val added_at: Timestamp,
    val category: String = "",
    val quote_id: String = ""
)