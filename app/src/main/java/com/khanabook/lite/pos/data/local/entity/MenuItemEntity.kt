package com.khanabook.lite.pos.data.local.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.*

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["category_id"])]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    val name: String,
    @ColumnInfo(name = "base_price")
    val basePrice: Double, // used only if no variants
    @ColumnInfo(name = "food_type", defaultValue = "veg")
    val foodType: String = "veg",
    val description: String? = null,
    @ColumnInfo(name = "is_available", defaultValue = "1")
    val isAvailable: Boolean = true,
    @ColumnInfo(name = "stock_quantity", defaultValue = "0")
    val stockQuantity: Int = 0,
    @ColumnInfo(name = "low_stock_threshold", defaultValue = "10")
    val lowStockThreshold: Int = 10,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)


