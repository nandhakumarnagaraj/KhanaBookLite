package com.khanabook.lite.pos.data.local.dao

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.MaterialBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: MaterialBatchEntity)

    @Update
    suspend fun updateBatch(batch: MaterialBatchEntity)

    @Query("SELECT * FROM material_batches WHERE raw_material_id = :materialId AND is_depleted = 0 ORDER BY expiry_date ASC")
    suspend fun getAvailableBatchesForMaterial(materialId: Int): List<MaterialBatchEntity>

    @Query("SELECT * FROM material_batches WHERE expiry_date <= :date AND is_depleted = 0")
    fun getExpiringBatches(date: String): Flow<List<MaterialBatchEntity>>

    @Query("SELECT * FROM material_batches WHERE raw_material_id = :materialId ORDER BY received_date DESC")
    fun getBatchesForMaterial(materialId: Int): Flow<List<MaterialBatchEntity>>
}
