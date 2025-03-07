package com.example.passionDaily.favorites.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.passionDaily.constants.DatabaseConstants
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity

@Entity(
    tableName = DatabaseConstants.TABLE_FAVORITES,
    primaryKeys = [DatabaseConstants.COLUMN_QUOTE_ID, DatabaseConstants.COLUMN_CATEGORY_ID],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = [DatabaseConstants.COLUMN_QUOTE_ID, DatabaseConstants.COLUMN_CATEGORY_ID],
            childColumns = [DatabaseConstants.COLUMN_QUOTE_ID, DatabaseConstants.COLUMN_CATEGORY_ID],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = [DatabaseConstants.COLUMN_CATEGORY_ID],
            childColumns = [DatabaseConstants.COLUMN_CATEGORY_ID],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = [DatabaseConstants.COLUMN_QUOTE_ID, DatabaseConstants.COLUMN_CATEGORY_ID]),
        Index(value = [DatabaseConstants.COLUMN_CATEGORY_ID])
    ]
)
data class FavoriteEntity(
    @ColumnInfo(name = DatabaseConstants.COLUMN_QUOTE_ID) val quoteId: String,
    @ColumnInfo(name = DatabaseConstants.COLUMN_CATEGORY_ID) val categoryId: Int,
    @ColumnInfo(name = DatabaseConstants.COLUMN_ADDED_AT) val addedAt: Long = System.currentTimeMillis()
)