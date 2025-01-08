package com.example.passionDaily.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    primaryKeys = ["user_id", "quote_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["quote_id"],
            childColumns = ["quote_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["quote_id"]),
        Index(value = ["category_id"])
    ]
)
data class FavoriteEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "quote_id") val quoteId: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)
