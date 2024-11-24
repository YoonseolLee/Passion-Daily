package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp

data class Term(
    val version: String = "",
    val type: TermType = TermType.SERVICE,
    val content: String = "",
    val createdDate: Timestamp = Timestamp.now()
) {
    enum class TermType {
        SERVICE, PRIVACY, MARKETING
    }
}
