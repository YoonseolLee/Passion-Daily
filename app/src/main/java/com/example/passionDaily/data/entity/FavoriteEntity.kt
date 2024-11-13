package com.example.passionDaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["quote_id"],
            childColumns = ["quote_id"],
            onDelete = ForeignKey.CASCADE,
        ),

    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["quote_id", "user_id"])
    ],
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "favorite_id") val favoriteId: Int = 0,
    @ColumnInfo(name = "quote_id") val quoteId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "created_date") val createdDate: Long,
)
