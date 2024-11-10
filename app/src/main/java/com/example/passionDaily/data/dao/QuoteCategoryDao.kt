package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.passionDaily.data.entity.QuoteCategoryEntity

@Dao
interface QuoteCategoryDao {
    @Query("SELECT * FROM quote_categories")
    fun getAllCategories(): List<QuoteCategoryEntity>
}
