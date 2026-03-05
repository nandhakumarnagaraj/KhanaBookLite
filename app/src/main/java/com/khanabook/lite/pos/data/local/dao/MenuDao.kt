package com.khanabook.lite.pos.data.local.dao

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.MenuItemEntity
import com.khanabook.lite.pos.data.local.entity.ItemVariantEntity
import com.khanabook.lite.pos.data.local.relation.MenuWithVariants
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    // Menu Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MenuItemEntity): Long

    @Update
    suspend fun updateItem(item: MenuItemEntity)

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getItemById(id: Int): MenuItemEntity?

    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItemsOnce(): List<MenuItemEntity>

    @Query("SELECT * FROM menu_items")
    fun getAllItemsFlow(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE category_id = :categoryId ORDER BY name ASC")
    fun getItemsByCategoryFlow(categoryId: Int): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE name LIKE :query OR category_id IN (SELECT id FROM categories WHERE name LIKE :query)")
    fun searchItems(query: String): Flow<List<MenuItemEntity>>

    @Query("UPDATE menu_items SET is_available = :isAvailable WHERE id = :id")
    suspend fun toggleItemAvailability(id: Int, isAvailable: Boolean)

    @Query("UPDATE menu_items SET stock_quantity = stock_quantity + :delta WHERE id = :id AND (stock_quantity + :delta >= 0)")
    suspend fun updateStock(id: Int, delta: Int)

    @Query("UPDATE menu_items SET low_stock_threshold = :threshold WHERE id = :id")
    suspend fun updateLowStockThreshold(id: Int, threshold: Int)

    @Delete
    suspend fun deleteItem(item: MenuItemEntity)

    // Item Variants
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ItemVariantEntity): Long

    @Update
    suspend fun updateVariant(variant: ItemVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: ItemVariantEntity)

    @Query("SELECT * FROM item_variants WHERE menu_item_id = :itemId ORDER BY sort_order ASC")
    fun getVariantsForItemFlow(itemId: Int): Flow<List<ItemVariantEntity>>

    @Transaction
    @Query("SELECT * FROM menu_items WHERE category_id = :categoryId ORDER BY name ASC")
    fun getMenuWithVariantsByCategoryFlow(categoryId: Int): Flow<List<MenuWithVariants>>
}


