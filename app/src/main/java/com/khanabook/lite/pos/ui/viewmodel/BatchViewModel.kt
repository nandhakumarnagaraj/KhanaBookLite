package com.khanabook.lite.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabook.lite.pos.data.local.entity.MaterialBatchEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.repository.BatchRepository
import com.khanabook.lite.pos.data.repository.RawMaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BatchViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val rawMaterialRepository: RawMaterialRepository
) : ViewModel() {

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("BatchViewModel", "Coroutine error: ${throwable.message}", throwable)
    }

    val rawMaterials: StateFlow<List<RawMaterialEntity>> = rawMaterialRepository.allRawMaterials
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedMaterialId = MutableStateFlow<Int?>(null)
    val selectedMaterialId: StateFlow<Int?> = _selectedMaterialId

    val batches: StateFlow<List<MaterialBatchEntity>> = _selectedMaterialId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else batchRepository.getBatchesForMaterial(id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val expiringSoon: StateFlow<List<MaterialBatchEntity>> = batchRepository.getExpiringSoon(7)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectMaterial(id: Int?) {
        _selectedMaterialId.value = id
    }

    fun addBatch(materialId: Int, quantity: Double, expiryDate: String, batchNumber: String?) {
        viewModelScope.launch(exceptionHandler) {
            batchRepository.addBatch(materialId, quantity, expiryDate, batchNumber)
        }
    }
}
