package com.example.passionDaily.quotecategory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity

@Dao
interface QuoteCategoryDao {
    @Query("SELECT * FROM quote_categories")
    suspend fun getAllCategories(): List<QuoteCategoryEntity>

    @Query("SELECT * FROM quote_categories WHERE category_id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): QuoteCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: QuoteCategoryEntity)

    @Update
    suspend fun updateCategory(category: QuoteCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: QuoteCategoryEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM quote_categories WHERE category_id = :categoryId)")
    suspend fun isCategoryExists(categoryId: Int): Boolean

    @Query("DELETE FROM quote_categories")
    suspend fun deleteAllCategories()
}
