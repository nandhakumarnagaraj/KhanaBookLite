package com.khanabooklite.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khanabooklite.app.data.local.entity.BillEntity
import com.khanabooklite.app.data.local.entity.BillItemEntity
import com.khanabooklite.app.data.local.entity.BillPaymentEntity

data class BillWithItems(
    @Embedded val bill: BillEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "bill_id"
    )
    val items: List<BillItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "bill_id"
    )
    val payments: List<BillPaymentEntity>
)
