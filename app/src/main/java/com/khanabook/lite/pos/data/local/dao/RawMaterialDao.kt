package com.khanabook.lite.pos.data.local.dao

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialStockLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RawMaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawMaterial(material: RawMaterialEntity): Long

    @Update
    suspend fun updateRawMaterial(material: RawMaterialEntity)

    @Delete
    suspend fun deleteRawMaterial(material: RawMaterialEntity)

    @Query("SELECT * FROM raw_materials ORDER BY name ASC")
    fun getAllRawMaterials(): Flow<List<RawMaterialEntity>>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    suspend fun getRawMaterialById(id: Int): RawMaterialEntity?

    @Query("UPDATE raw_materials SET current_stock = current_stock + :delta, last_updated = :now WHERE id = :id")
    suspend fun updateStock(id: Int, delta: Double, now: String)

    @Query("UPDATE raw_materials SET low_stock_threshold = :threshold WHERE id = :id")
    suspend fun updateLowStockThreshold(id: Int, threshold: Double)

    @Insert
    suspend fun insertStockLog(log: RawMaterialStockLogEntity)

    @Query("SELECT * FROM raw_material_stock_logs WHERE raw_material_id = :materialId ORDER BY created_at DESC")
    fun getLogsForMaterial(materialId: Int): Flow<List<RawMaterialStockLogEntity>>

    @Query("SELECT * FROM raw_material_stock_logs ORDER BY created_at DESC LIMIT 200")
    fun getAllLogs(): Flow<List<RawMaterialStockLogEntity>>
}
