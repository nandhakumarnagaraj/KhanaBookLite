package com.khanabook.lite.pos.data.local.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.*

@Entity(
    tableName = "item_variants",
    foreignKeys = [
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["menu_item_id"])]
)
data class ItemVariantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "menu_item_id")
    val menuItemId: Int,
    @ColumnInfo(name = "variant_name")
    val variantName: String, // e.g. "Half", "Full", "Party Pack"
    val price: Double,
    @ColumnInfo(name = "is_available", defaultValue = "1")
    val isAvailable: Boolean = true,
    @ColumnInfo(name = "sort_order", defaultValue = "0")
    val sortOrder: Int = 0
)


