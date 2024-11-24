package com.example.passionDaily.data.remote.model

data class Author(
    val id: String = "",
    val name: String = "",
    val isDeleted: Boolean = false,
    val quoteCount: Int = 0
)