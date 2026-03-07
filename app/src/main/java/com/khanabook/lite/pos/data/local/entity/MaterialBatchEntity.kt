package com.khanabook.lite.pos.data.local.entity

import androidx.room.*

@Entity(
    tableName = "material_batches",
    foreignKeys = [
        ForeignKey(
            entity = RawMaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["raw_material_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("raw_material_id")]
)
data class MaterialBatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "raw_material_id")
    val rawMaterialId: Int,
    val quantity: Double,
    @ColumnInfo(name = "initial_quantity")
    val initialQuantity: Double,
    @ColumnInfo(name = "expiry_date")
    val expiryDate: String, // yyyy-MM-dd
    @ColumnInfo(name = "received_date")
    val receivedDate: String,
    @ColumnInfo(name = "batch_number")
    val batchNumber: String? = null,
    @ColumnInfo(name = "is_depleted", defaultValue = "0")
    val isDepleted: Boolean = false
)
