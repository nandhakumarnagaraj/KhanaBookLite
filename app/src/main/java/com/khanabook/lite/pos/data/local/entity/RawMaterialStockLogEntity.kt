package com.khanabook.lite.pos.data.local.entity

import androidx.room.*

@Entity(
    tableName = "raw_material_stock_logs",
    foreignKeys = [
        ForeignKey(
            entity = RawMaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["raw_material_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["raw_material_id"])]
)
data class RawMaterialStockLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "raw_material_id")
    val rawMaterialId: Int,
    val delta: Double,
    val reason: String, // 'purchase', 'usage', 'wastage', 'initial'
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
