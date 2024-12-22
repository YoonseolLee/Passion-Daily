package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Quote(
    val id: String,
    val category: String,
    val text: String,
    val person: String,
    val imageUrl: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val isDeleted: Boolean,
    val shareCount: Int
)
