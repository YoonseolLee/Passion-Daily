package com.example.passionDaily.data.local.dto

import androidx.room.ColumnInfo

data class FavoriteWithCategory(
    @ColumnInfo(name = "quote_id")
    val quoteId: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Int
)