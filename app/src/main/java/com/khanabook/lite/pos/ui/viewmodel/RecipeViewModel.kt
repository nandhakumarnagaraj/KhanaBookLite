package com.khanabook.lite.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabook.lite.pos.data.local.entity.MenuItemEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.local.entity.RecipeIngredientEntity
import com.khanabook.lite.pos.data.repository.MenuRepository
import com.khanabook.lite.pos.data.repository.RawMaterialRepository
import com.khanabook.lite.pos.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val rawMaterialRepository: RawMaterialRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("RecipeViewModel", "Coroutine error: ${throwable.message}", throwable)
    }

    val menuItems: StateFlow<List<MenuItemEntity>> = menuRepository.getAllItemsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val rawMaterials: StateFlow<List<RawMaterialEntity>> = rawMaterialRepository.allRawMaterials
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyInList())

    private val _selectedMenuItemId = MutableStateFlow<Int?>(null)
    val selectedMenuItemId: StateFlow<Int?> = _selectedMenuItemId

    val currentRecipeIngredients: StateFlow<List<RecipeIngredientEntity>> = _selectedMenuItemId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else recipeRepository.getIngredientsForMenuItem(id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun <T> emptyInList(): List<T> = emptyList() // helper to avoid type inference issues in flow

    fun selectMenuItem(id: Int?) {
        _selectedMenuItemId.value = id
    }

    fun addIngredient(menuItemId: Int, rawMaterialId: Int, quantity: Double) {
        viewModelScope.launch(exceptionHandler) {
            recipeRepository.addIngredient(menuItemId, rawMaterialId, quantity)
        }
    }

    fun removeIngredient(ingredient: RecipeIngredientEntity) {
        viewModelScope.launch(exceptionHandler) {
            recipeRepository.removeIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: RecipeIngredientEntity, newQuantity: Double) {
        viewModelScope.launch(exceptionHandler) {
            recipeRepository.updateIngredient(ingredient.copy(quantityNeeded = newQuantity))
        }
    }
}
