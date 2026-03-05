package com.khanabook.lite.pos.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import com.khanabook.lite.pos.data.local.dao.CategoryDao
import com.khanabook.lite.pos.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategoriesFlow()
    }

    fun getActiveCategoriesFlow(): Flow<List<CategoryEntity>> {
        return categoryDao.getActiveCategoriesFlow()
    }

    suspend fun toggleActive(id: Int, isActive: Boolean) {
        categoryDao.toggleActive(id, isActive)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
    
    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }
}


