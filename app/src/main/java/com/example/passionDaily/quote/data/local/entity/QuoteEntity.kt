package com.example.passionDaily.quote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.passionDaily.constants.DatabaseConstants
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity

@Entity(
    tableName = DatabaseConstants.TABLE_QUOTES,
    primaryKeys = [DatabaseConstants.COLUMN_QUOTE_ID, DatabaseConstants.COLUMN_CATEGORY_ID],
    foreignKeys = [
        ForeignKey(
            entity = QuoteCategoryEntity::class,
            parentColumns = [DatabaseConstants.COLUMN_CATEGORY_ID],
            childColumns = [DatabaseConstants.COLUMN_CATEGORY_ID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(DatabaseConstants.COLUMN_CATEGORY_ID),
        Index(
            value = [DatabaseConstants.COLUMN_QUOTE_ID, DatabaseConstants.COLUMN_CATEGORY_ID],
            unique = true
        )
    ]
)
data class QuoteEntity(
    @ColumnInfo(name = DatabaseConstants.COLUMN_QUOTE_ID) val quoteId: String,
    @ColumnInfo(name = DatabaseConstants.COLUMN_TEXT) val text: String,
    @ColumnInfo(name = DatabaseConstants.COLUMN_PERSON) val person: String,
    @ColumnInfo(name = DatabaseConstants.COLUMN_IMAGE_URL) val imageUrl: String,
    @ColumnInfo(name = DatabaseConstants.COLUMN_CATEGORY_ID) val categoryId: Int,
)
