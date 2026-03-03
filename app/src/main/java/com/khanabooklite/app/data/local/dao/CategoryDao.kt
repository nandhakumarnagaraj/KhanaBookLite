package com.khanabooklite.app.data.local.dao

import androidx.room.*
import com.khanabooklite.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE is_active = 1 ORDER BY sort_order ASC")
    fun getActiveCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("UPDATE categories SET is_active = :isActive WHERE id = :id")
    suspend fun toggleActive(id: Int, isActive: Boolean)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)
}
