package com.example.passionDaily.quotecategory.data.local.repository

import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity

interface LocalQuoteCategoryRepository {
    suspend fun getAllCategories(): List<QuoteCategoryEntity>
    suspend fun getCategoryById(categoryId: Int): QuoteCategoryEntity?
    suspend fun insertCategory(category: QuoteCategoryEntity)
    suspend fun updateCategory(category: QuoteCategoryEntity)
    suspend fun deleteCategory(category: QuoteCategoryEntity)
    suspend fun isCategoryExists(categoryId: Int): Boolean
    suspend fun deleteAllCategories()
}