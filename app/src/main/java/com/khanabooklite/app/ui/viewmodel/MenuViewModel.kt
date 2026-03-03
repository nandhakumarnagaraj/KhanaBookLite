package com.khanabooklite.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.entity.CategoryEntity
import com.khanabooklite.app.data.local.entity.ItemVariantEntity
import com.khanabooklite.app.data.local.entity.MenuItemEntity
import com.khanabooklite.app.data.local.relation.MenuWithVariants
import com.khanabooklite.app.data.repository.CategoryRepository
import com.khanabooklite.app.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategoriesFlow()
        .onEach { list ->
            if (_selectedCategoryId.value == null && list.isNotEmpty()) {
                _selectedCategoryId.value = list.first().id
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId

    // Debounced search to prevent UI jitter and unnecessary recompositions
    private val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    val menuItems: StateFlow<List<MenuWithVariants>> = combine(_selectedCategoryId, debouncedSearchQuery) { id, query ->
        id to query
    }.flatMapLatest { (id, query) ->
        if (id != null) {
            menuRepository.getMenuWithVariantsByCategoryFlow(id).map { items ->
                val filteredByStock = items.filter { it.menuItem.stockQuantity > 0 }
                if (query.isBlank()) filteredByStock
                else filteredByStock.filter { it.menuItem.name.contains(query, ignoreCase = true) }
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Asynchronous reactive counting to avoid ANR risks
    val disabledItemsCount: StateFlow<Int> = menuRepository.getAllItemsFlow()
        .map { items -> items.count { !it.isAvailable } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val menuAddOnsCount: StateFlow<Int> = categories.map { list ->
        // Priority based filtering for add-ons
        list.count { cat -> 
            val name = cat.name.lowercase()
            name.contains("add-on") || name.contains("extra") || name.contains("side") || name.contains("combo")
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(id: Int) {
        _selectedCategoryId.value = id
    }

    fun toggleCategory(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            categoryRepository.toggleActive(id, isActive)
        }
    }

    fun toggleItem(id: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            menuRepository.toggleItemAvailability(id, isAvailable)
        }
    }

    fun addCategory(name: String, isVeg: Boolean) {
        viewModelScope.launch {
            // Check for duplicate category name (case-insensitive)
            val isDuplicate = categories.value.any { it.name.equals(name, ignoreCase = true) }
            if (isDuplicate) {
                // Silently skip or we could add an error state flow here
                return@launch
            }
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            categoryRepository.insertCategory(
                CategoryEntity(
                    name = name,
                    isVeg = isVeg,
                    createdAt = sdf.format(Date())
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    fun addItem(categoryId: Int, name: String, price: Double, foodType: String, stock: Int = 0, threshold: Int = 10) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            menuRepository.insertItem(
                MenuItemEntity(
                    categoryId = categoryId,
                    name = name,
                    basePrice = price,
                    foodType = foodType,
                    stockQuantity = stock,
                    lowStockThreshold = threshold,
                    createdAt = sdf.format(Date())
                )
            )
        }
    }

    fun updateItem(item: MenuItemEntity) {
        viewModelScope.launch {
            menuRepository.updateItem(item)
        }
    }

    fun deleteItem(item: MenuItemEntity) {
        viewModelScope.launch {
            menuRepository.deleteItem(item)
        }
    }

    fun addVariant(menuItemId: Int, name: String, price: Double) {
        viewModelScope.launch {
            menuRepository.insertVariant(
                ItemVariantEntity(
                    menuItemId = menuItemId,
                    variantName = name,
                    price = price,
                    sortOrder = 0
                )
            )
        }
    }

    fun updateVariant(variant: ItemVariantEntity) {
        viewModelScope.launch {
            menuRepository.updateVariant(variant)
        }
    }

    fun deleteVariant(variant: ItemVariantEntity) {
        viewModelScope.launch {
            menuRepository.deleteVariant(variant)
        }
    }
}
