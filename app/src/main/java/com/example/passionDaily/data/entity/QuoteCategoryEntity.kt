package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "quote_categories",
    foreignKeys = [
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["parentCategoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryName"], unique = true),
        Index(value = ["parentCategoryId"])
    ]
)
data class QuoteCategoryEntity(
    @PrimaryKey val categoryId: Int,
    val categoryName: String,
    val parentCategoryId: Int?,
    val createdDate: Date,
    val modifiedDate: Date,
)
