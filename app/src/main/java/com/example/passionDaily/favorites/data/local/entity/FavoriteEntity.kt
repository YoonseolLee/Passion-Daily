package com.example.passionDaily.favorites.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity

@Entity(
    tableName = "favorites",
    primaryKeys = ["quote_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["quote_id", "category_id"],
            childColumns = ["quote_id", "category_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["quote_id", "category_id"]),
        Index(value = ["category_id"])
    ]
)
data class FavoriteEntity(
    @ColumnInfo(name = "quote_id") val quoteId: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)
