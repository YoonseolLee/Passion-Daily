package com.example.passionDaily.favorites.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.user.data.local.entity.UserEntity

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
        Index(value = ["user_id"]),
        Index(value = ["quote_id", "category_id"]),
        Index(value = ["category_id"])
    ]
)
data class FavoriteEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "quote_id") val quoteId: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)
