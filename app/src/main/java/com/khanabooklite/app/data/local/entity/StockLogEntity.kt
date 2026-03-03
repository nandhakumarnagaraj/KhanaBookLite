package com.khanabooklite.app.data.local.entity

import androidx.room.*

@Entity(
    tableName = "stock_logs",
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
data class StockLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "menu_item_id")
    val menuItemId: Int,
    val delta: Int,
    val reason: String, // 'sale', 'adjustment', 'initial'
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
