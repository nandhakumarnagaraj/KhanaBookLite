package com.khanabooklite.app.data.local.entity

import androidx.room.*

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["bill_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ItemVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variant_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["bill_id"]),
        Index(value = ["menu_item_id"]),
        Index(value = ["variant_id"])
    ]
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "bill_id")
    val billId: Int,
    @ColumnInfo(name = "menu_item_id")
    val menuItemId: Int?,
    @ColumnInfo(name = "item_name")
    val itemName: String, // snapshot
    @ColumnInfo(name = "variant_id")
    val variantId: Int? = null,
    @ColumnInfo(name = "variant_name")
    val variantName: String? = null, // snapshot
    val price: Double, // snapshot
    val quantity: Int,
    @ColumnInfo(name = "item_total")
    val itemTotal: Double, // price * qty
    @ColumnInfo(name = "special_instruction")
    val specialInstruction: String? = null
)
