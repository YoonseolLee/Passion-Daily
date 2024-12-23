package com.example.passionDaily.data.remote.model

import java.sql.Timestamp

data class Terms(
    val createdDate: Timestamp,
    val modifiedDate: Timestamp,
    val title: String,
    val url: String,
    val version: String
)
