package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "quote_categories")
data class QuoteCategoryEntity(
    @PrimaryKey val categoryId: Int,
    val categoryName: String,
    val parentCategoryId: Int?,
    val createdDate: Date,
    val modifiedDate: Date,
)
