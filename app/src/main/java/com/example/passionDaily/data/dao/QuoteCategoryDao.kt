package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.relation.CategoryWithQuotes

@Dao
interface QuoteCategoryDao {
    @Query("SELECT * FROM quote_categories")
    fun getAllCategories(): List<QuoteCategoryEntity>

    @Transaction
    @Query("SELECT * FROM quote_categories")
    fun getCategoriesWithQuotes(): List<CategoryWithQuotes>
}
