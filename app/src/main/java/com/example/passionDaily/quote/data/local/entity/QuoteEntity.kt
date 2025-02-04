package com.example.passionDaily.quote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity

@Entity(
    tableName = "quotes",
    primaryKeys = ["quote_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("category_id"),
        Index(value = ["quote_id", "category_id"], unique = true)
    ]
)
data class QuoteEntity(
    @ColumnInfo(name = "quote_id") val quoteId: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "person") val person: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
)
