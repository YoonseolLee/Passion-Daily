package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import javax.inject.Inject

class LocalQuoteCategoryRepositoryImpl @Inject constructor(
    private val quoteCategoryDao: QuoteCategoryDao
) : LocalQuoteCategoryRepository {

    override suspend fun getAllCategories(): List<QuoteCategoryEntity> {
        return quoteCategoryDao.getAllCategories()
    }

    override suspend fun getCategoryById(categoryId: Int): QuoteCategoryEntity? {
        return quoteCategoryDao.getCategoryById(categoryId)
    }

    override suspend fun insertCategory(category: QuoteCategoryEntity) {
        return quoteCategoryDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: QuoteCategoryEntity) {
        return quoteCategoryDao.updateCategory(category)
    }

    override suspend fun deleteCategory(category: QuoteCategoryEntity) {
        return quoteCategoryDao.deleteCategory(category)
    }

    override suspend fun isCategoryExists(categoryId: Int): Boolean {
        return quoteCategoryDao.isCategoryExists(categoryId)
    }

    override suspend fun deleteAllCategories() {
        return quoteCategoryDao.deleteAllCategories()
    }
}