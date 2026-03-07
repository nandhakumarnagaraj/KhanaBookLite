package com.khanabook.lite.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialStockLogEntity
import com.khanabook.lite.pos.data.repository.RawMaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val rawMaterialRepository: RawMaterialRepository
) : ViewModel() {

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("InventoryViewModel", "Coroutine error: ${throwable.message}", throwable)
    }

    val rawMaterials: StateFlow<List<RawMaterialEntity>> = rawMaterialRepository.allRawMaterials
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedMaterialId = MutableStateFlow<Int?>(null)
    val selectedMaterialId: StateFlow<Int?> = _selectedMaterialId

    val stockLogs: StateFlow<List<RawMaterialStockLogEntity>> = _selectedMaterialId
        .flatMapLatest { id ->
            if (id == null) rawMaterialRepository.getAllLogs()
            else rawMaterialRepository.getLogsForMaterial(id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectMaterial(materialId: Int?) {
        _selectedMaterialId.value = materialId
    }

    fun addRawMaterial(name: String, unit: String, initialStock: Double, threshold: Double) {
        viewModelScope.launch(exceptionHandler) {
            rawMaterialRepository.addRawMaterial(name, unit, initialStock, threshold)
        }
    }

    fun adjustStock(materialId: Int, delta: Double, reason: String) {
        viewModelScope.launch(exceptionHandler) {
            rawMaterialRepository.adjustStock(materialId, delta, reason)
        }
    }

    fun updateThreshold(materialId: Int, threshold: Double) {
        viewModelScope.launch(exceptionHandler) {
            rawMaterialRepository.updateThreshold(materialId, threshold)
        }
    }

    fun deleteMaterial(material: RawMaterialEntity) {
        viewModelScope.launch(exceptionHandler) {
            rawMaterialRepository.deleteMaterial(material)
        }
    }
}
