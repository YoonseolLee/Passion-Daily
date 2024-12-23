package com.example.passionDaily.data.remote.model

import java.sql.Timestamp

data class Notification(
    val createdAt: Timestamp,
    val isDeleted: Boolean,
    val message: String,
    val quoteId: String,
    val scheduledTime: Timestamp,
    val sentTime: Timestamp,
    val status: String,
    val title: String,
    val type: String
)
