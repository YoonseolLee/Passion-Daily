package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import javax.inject.Inject

class QuoteCategoryRepository @Inject constructor(
    private val quoteCategoryDao: QuoteCategoryDao
) {
    suspend fun getAllCategories(): List<QuoteCategoryEntity> {
        return quoteCategoryDao.getAllCategories()
    }

    suspend fun getCategoryById(categoryId: Int): QuoteCategoryEntity? {
        return quoteCategoryDao.getCategoryById(categoryId)
    }

    suspend fun insertCategory(category: QuoteCategoryEntity) {
        return quoteCategoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: QuoteCategoryEntity) {
        return quoteCategoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: QuoteCategoryEntity) {
        return quoteCategoryDao.deleteCategory(category)
    }
}