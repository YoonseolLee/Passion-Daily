package com.example.passionDaily.data.remote.model

data class Quote(
    val id: String,
    val category: String,
    val text: String,
    val person: String,
    val imageUrl: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val shareCount: Int
)
