package com.khanabooklite.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.relation.BillWithItems
import com.khanabooklite.app.data.repository.BillRepository
import com.khanabooklite.app.domain.manager.SearchManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    private val searchManager = SearchManager(billRepository)

    private val _searchResult = MutableStateFlow<BillWithItems?>(null)
    val searchResult: StateFlow<BillWithItems?> = _searchResult

    fun searchByDailyId(displayId: String, date: String) {
        viewModelScope.launch {
            _searchResult.value = searchManager.searchByDailyId(displayId, date)
        }
    }

    fun searchByLifetimeId(id: Int) {
        viewModelScope.launch {
            _searchResult.value = searchManager.searchByLifetimeId(id)
        }
    }

    fun clearSearch() {
        _searchResult.value = null
    }

    fun updatePaymentMode(billId: Int, newMode: String) {
        viewModelScope.launch {
            billRepository.updatePaymentMode(billId, newMode)
            // Refresh the current search result to reflect changes
            _searchResult.value?.let { current ->
                if (current.bill.id == billId) {
                    _searchResult.value = billRepository.getBillWithItemsById(billId)
                }
            }
        }
    }
}
