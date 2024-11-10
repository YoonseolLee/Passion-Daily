package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val quoteId: Int,
    val text: String,
    val author: String?,
    val imageUrl: String?,
    val categoryId: Int,
    val views: Int,
    val likes: Int,
    val createdDate: Date,
    val modifiedDate: Date,
)
