package com.khanabook.lite.pos.data.repository

import com.khanabook.lite.pos.data.local.dao.RawMaterialDao
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialStockLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RawMaterialRepository @Inject constructor(
    private val rawMaterialDao: RawMaterialDao
) {
    val allRawMaterials: Flow<List<RawMaterialEntity>> = rawMaterialDao.getAllRawMaterials()

    suspend fun addRawMaterial(name: String, unit: String, initialStock: Double, threshold: Double) {
        val now = getCurrentTimestamp()
        val material = RawMaterialEntity(
            name = name,
            unit = unit,
            currentStock = initialStock,
            lowStockThreshold = threshold,
            lastUpdated = now
        )
        val id = rawMaterialDao.insertRawMaterial(material).toInt()
        
        if (initialStock != 0.0) {
            rawMaterialDao.insertStockLog(
                RawMaterialStockLogEntity(
                    rawMaterialId = id,
                    delta = initialStock,
                    reason = "initial",
                    createdAt = now
                )
            )
        }
    }

    suspend fun adjustStock(materialId: Int, delta: Double, reason: String) {
        val now = getCurrentTimestamp()
        rawMaterialDao.updateStock(materialId, delta, now)
        rawMaterialDao.insertStockLog(
            RawMaterialStockLogEntity(
                rawMaterialId = materialId,
                delta = delta,
                reason = reason,
                createdAt = now
            )
        )
    }

    suspend fun updateThreshold(materialId: Int, threshold: Double) {
        rawMaterialDao.updateLowStockThreshold(materialId, threshold)
    }

    suspend fun deleteMaterial(material: RawMaterialEntity) {
        rawMaterialDao.deleteRawMaterial(material)
    }

    fun getLogsForMaterial(materialId: Int): Flow<List<RawMaterialStockLogEntity>> = 
        rawMaterialDao.getLogsForMaterial(materialId)

    fun getAllLogs(): Flow<List<RawMaterialStockLogEntity>> = rawMaterialDao.getAllLogs()

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
