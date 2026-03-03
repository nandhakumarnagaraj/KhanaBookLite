package com.khanabooklite.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.entity.MenuItemEntity
import com.khanabooklite.app.data.local.entity.StockLogEntity
import com.khanabooklite.app.data.repository.InventoryRepository
import com.khanabooklite.app.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    val menuItems: StateFlow<List<MenuItemEntity>> = menuRepository.getAllItemsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedItemId = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = _selectedItemId

    val stockLogs: StateFlow<List<StockLogEntity>> = _selectedItemId
        .flatMapLatest { id ->
            if (id == null) inventoryRepository.getAllLogs()
            else inventoryRepository.getLogsForItem(id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectItem(itemId: Int?) {
        _selectedItemId.value = itemId
    }

    fun adjustStock(itemId: Int, delta: Int, reason: String) {
        viewModelScope.launch {
            inventoryRepository.adjustStock(itemId, delta, reason)
        }
    }

    fun updateThreshold(itemId: Int, threshold: Int) {
        viewModelScope.launch {
            inventoryRepository.updateThreshold(itemId, threshold)
        }
    }
}
