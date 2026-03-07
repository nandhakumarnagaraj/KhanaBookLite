package com.khanabook.lite.pos.data.local.dao

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: RecipeIngredientEntity)

    @Update
    suspend fun updateIngredient(ingredient: RecipeIngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: RecipeIngredientEntity)

    @Query("SELECT * FROM recipe_ingredients WHERE menu_item_id = :menuItemId")
    fun getIngredientsForMenuItem(menuItemId: Int): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients WHERE menu_item_id = :menuItemId")
    suspend fun getIngredientsForMenuItemOnce(menuItemId: Int): List<RecipeIngredientEntity>
}
