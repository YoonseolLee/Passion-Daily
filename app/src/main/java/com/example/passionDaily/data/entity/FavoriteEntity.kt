package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["quoteId"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("quoteId"),
        Index("userId"),
        Index(value = ["quoteId", "userId"], unique = true)  // 중복 즐겨찾기 방지
    ])
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val favoriteId: Int = 0,
    val quoteId: Int,
    val userId: Int,
    val createdDate: Date,
)
