package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp

data class Promotion(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val isDeleted: Boolean = false,
    val createdDate: Timestamp = Timestamp.now(),
    val modifiedDate: Timestamp = Timestamp.now()
)