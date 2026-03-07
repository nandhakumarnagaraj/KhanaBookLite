package com.khanabook.lite.pos.domain.manager

import com.khanabook.lite.pos.data.local.entity.BillItemEntity
import com.khanabook.lite.pos.data.repository.BatchRepository
import com.khanabook.lite.pos.data.repository.RecipeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryConsumptionManager @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val batchRepository: BatchRepository
) {
    /**
     * Deducts raw materials from inventory based on the items in a bill.
     */
    suspend fun consumeMaterialsForBill(items: List<BillItemEntity>) {
        for (item in items) {
            val itemId = item.menuItemId ?: continue
            val ingredients = recipeRepository.getIngredientsOnce(itemId)
            for (ingredient in ingredients) {
                val totalToConsume = ingredient.quantityNeeded * item.quantity
                batchRepository.consumeFromBatches(
                    materialId = ingredient.rawMaterialId,
                    totalToConsume = totalToConsume,
                    reason = "Sale (Bill #${item.billId})"
                )
            }
        }
    }
}
