package com.example.passionDaily.quotecategory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_categories")
data class QuoteCategoryEntity(
    @PrimaryKey @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "category_name") val categoryName: String,
)
