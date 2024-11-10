package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "quotes",
    foreignKeys = [
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["catrgoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId")]
)
data class QuoteEntity(
    @PrimaryKey val quoteId: Int,
    val text: String,
    val author: String?,
    val imageUrl: String?,
    val categoryId: Int,
    val views: Int = 0,
    val likes: Int = 0,
    val createdDate: Date,
    val modifiedDate: Date,
)
