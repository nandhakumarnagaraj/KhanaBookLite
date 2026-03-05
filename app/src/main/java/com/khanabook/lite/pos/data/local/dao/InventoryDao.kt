package com.khanabook.lite.pos.data.local.dao

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.StockLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert
    suspend fun insertStockLog(log: StockLogEntity)

    @Query("SELECT * FROM stock_logs WHERE menu_item_id = :itemId ORDER BY created_at DESC")
    fun getLogsForItem(itemId: Int): Flow<List<StockLogEntity>>

    @Query("SELECT * FROM stock_logs ORDER BY created_at DESC LIMIT 100")
    fun getAllLogs(): Flow<List<StockLogEntity>>
}


