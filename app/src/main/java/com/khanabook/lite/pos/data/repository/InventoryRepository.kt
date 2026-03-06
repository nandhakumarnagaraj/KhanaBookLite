package com.khanabook.lite.pos.data.repository

import com.khanabook.lite.pos.data.local.dao.InventoryDao
import com.khanabook.lite.pos.data.local.dao.MenuDao
import com.khanabook.lite.pos.data.local.entity.StockLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val menuDao: MenuDao
) {
    suspend fun adjustStock(menuItemId: Int, delta: Int, reason: String) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        menuDao.updateStock(menuItemId, delta)
        inventoryDao.insertStockLog(
            StockLogEntity(
                menuItemId = menuItemId,
                delta = delta,
                reason = reason,
                createdAt = now
            )
        )
    }

    suspend fun updateThreshold(menuItemId: Int, threshold: Int) {
        menuDao.updateLowStockThreshold(menuItemId, threshold)
    }

    fun getLogsForItem(itemId: Int): Flow<List<StockLogEntity>> = inventoryDao.getLogsForItem(itemId)
    
    fun getAllLogs(): Flow<List<StockLogEntity>> = inventoryDao.getAllLogs()
}


