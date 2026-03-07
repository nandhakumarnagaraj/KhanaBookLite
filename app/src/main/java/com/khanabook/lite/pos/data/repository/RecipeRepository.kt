package com.khanabook.lite.pos.data.repository

import com.khanabook.lite.pos.data.local.dao.RecipeDao
import com.khanabook.lite.pos.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {
    fun getIngredientsForMenuItem(menuItemId: Int): Flow<List<RecipeIngredientEntity>> =
        recipeDao.getIngredientsForMenuItem(menuItemId)

    suspend fun addIngredient(menuItemId: Int, rawMaterialId: Int, quantity: Double) {
        recipeDao.insertIngredient(
            RecipeIngredientEntity(
                menuItemId = menuItemId,
                rawMaterialId = rawMaterialId,
                quantityNeeded = quantity
            )
        )
    }

    suspend fun removeIngredient(ingredient: RecipeIngredientEntity) {
        recipeDao.deleteIngredient(ingredient)
    }

    suspend fun updateIngredient(ingredient: RecipeIngredientEntity) {
        recipeDao.updateIngredient(ingredient)
    }

    suspend fun getIngredientsOnce(menuItemId: Int): List<RecipeIngredientEntity> =
        recipeDao.getIngredientsForMenuItemOnce(menuItemId)
}
