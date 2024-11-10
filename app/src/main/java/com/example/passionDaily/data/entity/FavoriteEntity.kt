package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["quoteId"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("quoteId")],
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val favoriteId: Int = 0,
    val quoteId: Int,
    val userId: Int,
    val createdDate: Long,
)
