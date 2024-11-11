package com.example.passionDaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "quotes",
    foreignKeys = [
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("category_id")],
)
data class QuoteEntity(
    @PrimaryKey @ColumnInfo(name = "quote_id") val quoteId: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "author") val author: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "views") val views: Int,
    @ColumnInfo(name = "likes") val likes: Int,
    @ColumnInfo(name = "created_date") val createdDate: Date,
    @ColumnInfo(name = "modified_date") val modifiedDate: Date,
)
