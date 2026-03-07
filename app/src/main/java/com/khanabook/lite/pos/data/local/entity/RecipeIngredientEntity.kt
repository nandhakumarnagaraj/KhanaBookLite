package com.khanabook.lite.pos.data.local.entity

import androidx.room.*

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RawMaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["raw_material_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("menu_item_id"), Index("raw_material_id")]
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "menu_item_id")
    val menuItemId: Int,
    @ColumnInfo(name = "raw_material_id")
    val rawMaterialId: Int,
    @ColumnInfo(name = "quantity_needed")
    val quantityNeeded: Double // e.g., 0.2 kg of Chicken for 1 Biryani
)
