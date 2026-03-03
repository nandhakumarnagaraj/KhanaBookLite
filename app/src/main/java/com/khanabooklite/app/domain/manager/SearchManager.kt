package com.khanabooklite.app.domain.manager

import android.content.Intent
import android.net.Uri
import com.khanabooklite.app.data.local.relation.BillWithItems
import com.khanabooklite.app.data.repository.BillRepository

class SearchManager(private val billRepository: BillRepository) {

    suspend fun searchByDailyId(displayId: String, date: String): BillWithItems? {
        val billEntity = displayId.toIntOrNull()?.let { intId ->
            billRepository.getBillByDailyIntIdAndDate(intId, date)
        } ?: billRepository.getBillByDailyIdAndDate(displayId, date)
        
        return billEntity?.let { billRepository.getBillWithItemsById(it.id) }
    }

    suspend fun searchByLifetimeId(id: Int): BillWithItems? {
        return billRepository.getBillWithItemsByLifetimeId(id)
    }

    fun buildCallIntent(whatsappNumber: String): Intent {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$whatsappNumber")
        return intent
    }
}
