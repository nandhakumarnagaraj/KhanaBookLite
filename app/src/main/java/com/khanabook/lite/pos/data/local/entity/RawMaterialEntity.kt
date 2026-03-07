package com.khanabook.lite.pos.data.local.entity

import androidx.room.*

@Entity(tableName = "raw_materials")
data class RawMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val unit: String, // kg, ltr, pcs, etc.
    @ColumnInfo(name = "current_stock", defaultValue = "0.0")
    val currentStock: Double = 0.0,
    @ColumnInfo(name = "low_stock_threshold", defaultValue = "5.0")
    val lowStockThreshold: Double = 5.0,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: String
)
