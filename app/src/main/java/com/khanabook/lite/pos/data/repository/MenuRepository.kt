package com.khanabook.lite.pos.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import com.khanabook.lite.pos.data.local.dao.MenuDao
import com.khanabook.lite.pos.data.local.entity.MenuItemEntity
import com.khanabook.lite.pos.data.local.entity.ItemVariantEntity
import com.khanabook.lite.pos.data.local.relation.MenuWithVariants
import kotlinx.coroutines.flow.Flow

class MenuRepository(private val menuDao: MenuDao) {
    
    suspend fun insertItem(item: MenuItemEntity): Long {
        return menuDao.insertItem(item)
    }

    suspend fun updateItem(item: MenuItemEntity) {
        menuDao.updateItem(item)
    }

    suspend fun getItemById(id: Int): MenuItemEntity? {
        return menuDao.getItemById(id)
    }

    fun getItemsByCategoryFlow(categoryId: Int): Flow<List<MenuItemEntity>> {
        return menuDao.getItemsByCategoryFlow(categoryId)
    }

    fun getAllItemsFlow(): Flow<List<MenuItemEntity>> {
        return menuDao.getAllItemsFlow()
    }
    
    fun getMenuWithVariantsByCategoryFlow(categoryId: Int): Flow<List<MenuWithVariants>> {
        return menuDao.getMenuWithVariantsByCategoryFlow(categoryId)
    }

    fun searchItems(query: String): Flow<List<MenuItemEntity>> {
        return menuDao.searchItems("%$query%")
    }

    suspend fun toggleItemAvailability(id: Int, isAvailable: Boolean) {
        menuDao.toggleItemAvailability(id, isAvailable)
    }

    suspend fun deleteItem(item: MenuItemEntity) {
        menuDao.deleteItem(item)
    }

    suspend fun insertVariant(variant: ItemVariantEntity): Long {
        return menuDao.insertVariant(variant)
    }

    suspend fun updateVariant(variant: ItemVariantEntity) {
        menuDao.updateVariant(variant)
    }

    suspend fun deleteVariant(variant: ItemVariantEntity) {
        menuDao.deleteVariant(variant)
    }

    fun getVariantsForItemFlow(itemId: Int): Flow<List<ItemVariantEntity>> {
        return menuDao.getVariantsForItemFlow(itemId)
    }
}


