package com.example.passionDaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quote_categories",
    foreignKeys = [
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["parentCategoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["categoryName"], unique = true),
        Index(value = ["parentCategoryId"]),
    ],
)
data class QuoteCategoryEntity(
    @PrimaryKey @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "created_date") val createdDate: Long,
    @ColumnInfo(name = "modified_date") val modifiedDate: Long,
)
