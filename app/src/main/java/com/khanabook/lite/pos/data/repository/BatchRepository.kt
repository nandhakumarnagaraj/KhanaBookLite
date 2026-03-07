package com.khanabook.lite.pos.data.repository

import com.khanabook.lite.pos.data.local.dao.BatchDao
import com.khanabook.lite.pos.data.local.dao.RawMaterialDao
import com.khanabook.lite.pos.data.local.entity.MaterialBatchEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialStockLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchRepository @Inject constructor(
    private val batchDao: BatchDao,
    private val rawMaterialDao: RawMaterialDao
) {
    fun getBatchesForMaterial(materialId: Int): Flow<List<MaterialBatchEntity>> =
        batchDao.getBatchesForMaterial(materialId)

    suspend fun addBatch(materialId: Int, quantity: Double, expiryDate: String, batchNumber: String? = null) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val batch = MaterialBatchEntity(
            rawMaterialId = materialId,
            quantity = quantity,
            initialQuantity = quantity,
            expiryDate = expiryDate,
            receivedDate = now,
            batchNumber = batchNumber
        )
        batchDao.insertBatch(batch)
        
        // Also update total stock in RawMaterialEntity
        rawMaterialDao.updateStock(materialId, quantity, now)
        rawMaterialDao.insertStockLog(
            RawMaterialStockLogEntity(
                rawMaterialId = materialId,
                delta = quantity,
                reason = "Purchase (Batch)",
                createdAt = now
            )
        )
    }

    /**
     * Consumes quantity from batches using FIFO (First-In, First-Out) logic based on expiry date.
     */
    suspend fun consumeFromBatches(materialId: Int, totalToConsume: Double, reason: String) {
        if (totalToConsume <= 0) return
        
        try {
            var remainingToConsume = totalToConsume
            val batches = batchDao.getAvailableBatchesForMaterial(materialId)
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            for (batch in batches) {
                if (remainingToConsume <= 0) break
                if (batch.quantity <= 0) continue // Skip empty batches just in case

                val consumeFromThisBatch = minOf(batch.quantity, remainingToConsume)
                val updatedBatch = batch.copy(
                    quantity = batch.quantity - consumeFromThisBatch,
                    isDepleted = (batch.quantity - consumeFromThisBatch) <= 0.0001 // Use epsilon for double
                )
                batchDao.updateBatch(updatedBatch)
                remainingToConsume -= consumeFromThisBatch
            }

            // Update main stock total
            rawMaterialDao.updateStock(materialId, -totalToConsume, now)
            rawMaterialDao.insertStockLog(
                RawMaterialStockLogEntity(
                    rawMaterialId = materialId,
                    delta = -totalToConsume,
                    reason = reason,
                    createdAt = now
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("BatchRepository", "Error consuming stock for material $materialId", e)
            // Rethrow or handle based on business requirement. 
            // In POS, we might want to log it but not crash the billing screen.
        }
    }

    fun getExpiringSoon(days: Int): Flow<List<MaterialBatchEntity>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return batchDao.getExpiringBatches(dateString)
    }
}
