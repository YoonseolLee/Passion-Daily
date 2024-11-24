package com.example.passionDaily.data.remote.model.user

import java.util.Date

data class UserTerm(
    val id: String = "", // term_id
    val termVersion: Int = 0,
    val termType: String = "",
    val consentDate: Date = Date()
)