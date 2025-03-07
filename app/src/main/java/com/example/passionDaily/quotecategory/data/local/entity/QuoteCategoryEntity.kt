package com.example.passionDaily.quotecategory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.passionDaily.constants.DatabaseConstants

@Entity(tableName = DatabaseConstants.TABLE_QUOTE_CATEGORIES)
data class QuoteCategoryEntity(
    @PrimaryKey @ColumnInfo(name = DatabaseConstants.COLUMN_CATEGORY_ID) val categoryId: Int,
    @ColumnInfo(name = DatabaseConstants.COLUMN_CATEGORY_NAME) val categoryName: String,
)
