package com.example.passionDaily.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "quote_categories",
)
data class QuoteCategoryEntity(
    @PrimaryKey @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "created_date") val createdDate: Long,
    @ColumnInfo(name = "modified_date") val modifiedDate: Long,
)
