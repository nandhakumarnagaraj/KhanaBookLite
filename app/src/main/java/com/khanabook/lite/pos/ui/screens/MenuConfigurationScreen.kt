package com.khanabook.lite.pos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.khanabook.lite.pos.data.local.entity.CategoryEntity
import com.khanabook.lite.pos.data.local.entity.ItemVariantEntity
import com.khanabook.lite.pos.data.local.entity.MenuItemEntity
import com.khanabook.lite.pos.data.local.relation.MenuWithVariants
import com.khanabook.lite.pos.ui.theme.*
import com.khanabook.lite.pos.ui.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuConfigurationScreen(
    onBack: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val disabledCount by viewModel.disabledItemsCount.collectAsState()
    val addOnsCount by viewModel.menuAddOnsCount.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showVariantsFor by remember { mutableStateOf<MenuWithVariants?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBrown1, DarkBrown2)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                }
                Text(
                    "Menu Configuration",
                    modifier = Modifier.weight(1f),
                    color = PrimaryGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                // Spacer to balance the back button
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Main Content Area (Parchment Paper)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = ParchmentBG),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search and Filter Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search for an item or category", fontSize = 12.sp, color = Color.Gray) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                            ),
                            singleLine = true
                        )

                        FilterBadge(
                            label = "MENU ADD ONS",
                            count = addOnsCount,
                            backgroundColor = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        )

                        FilterBadge(
                            label = "DISABLED",
                            count = disabledCount,
                            backgroundColor = Color(0xFF757575),
                            icon = Icons.Default.VisibilityOff,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))

                    // Two Column Layout
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Category Column (Left)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CATEGORY (${categories.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("ADD NEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), modifier = Modifier.clickable { showAddCategoryDialog = true })
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(categories) { category ->
                                    CategoryItemRow(
                                        category = category,
                                        isSelected = selectedCategoryId == category.id,
                                        onClick = { viewModel.selectCategory(category.id) },
                                        onToggle = { viewModel.toggleCategory(category.id, it) },
                                        onDelete = {
                                            viewModel.deleteCategory(category)
                                            android.widget.Toast.makeText(context, "\"${category.name}\" category deleted", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        // Vertical Divider
                        VerticalDivider(color = Color.Black.copy(alpha = 0.1f))

                        // Item Column (Right)
                        Column(modifier = Modifier.weight(1.5f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ITEM (${menuItems.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(menuItems) { itemWithVariants ->
                                        MenuItemRow(
                                            itemWithVariants = itemWithVariants,
                                            onClick = { editingItem = itemWithVariants.menuItem },
                                            onToggle = { viewModel.toggleItem(itemWithVariants.menuItem.id, it) },
                                            onManageVariants = { showVariantsFor = itemWithVariants },
                                            onDelete = {
                                                viewModel.deleteItem(itemWithVariants.menuItem)
                                                android.widget.Toast.makeText(context, "\"${itemWithVariants.menuItem.name}\" deleted", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                                
                                // Large Add New Button at bottom right
                                val canAddItem = categories.isNotEmpty() && selectedCategoryId != null
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .clickable(enabled = canAddItem) { showAddItemDialog = true },
                                    color = if (canAddItem) ParchmentBG else Color.LightGray.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (canAddItem) Color(0xFF5D4037) else Color.Gray),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "ADD NEW", 
                                            color = if (canAddItem) Color(0xFF5D4037) else Color.Gray, 
                                            fontSize = 14.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, isVeg ->
                viewModel.addCategory(name, isVeg)
                android.widget.Toast.makeText(context, "\"$name\" category added", android.widget.Toast.LENGTH_SHORT).show()
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddItemDialog) {
        ItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, price, foodType, stock, threshold ->
                selectedCategoryId?.let { viewModel.addItem(it, name, price, foodType, stock, threshold) }
                android.widget.Toast.makeText(context, "\"$name\" added to menu", android.widget.Toast.LENGTH_SHORT).show()
                showAddItemDialog = false
            }
        )
    }

    showVariantsFor?.let { itemWithVariants ->
        ManageVariantsDialog(
            itemWithVariants = itemWithVariants,
            onDismiss = { showVariantsFor = null },
            onAddVariant = { name, price ->
                viewModel.addVariant(itemWithVariants.menuItem.id, name, price)
                android.widget.Toast.makeText(context, "\"$name\" variant added", android.widget.Toast.LENGTH_SHORT).show()
            },
            onUpdateVariant = { variant ->
                viewModel.updateVariant(variant)
                android.widget.Toast.makeText(context, "\"${variant.variantName}\" updated", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDeleteVariant = { variant ->
                viewModel.deleteVariant(variant)
                android.widget.Toast.makeText(context, "\"${variant.variantName}\" variant removed", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    editingItem?.let { item ->
        ItemDialog(
            initialItem = item,
            onDismiss = { editingItem = null },
            onConfirm = { name, price, foodType, stock, threshold ->
                viewModel.updateItem(
                    item.copy(
                        name = name,
                        basePrice = price,
                        foodType = foodType,
                        stockQuantity = stock,
                        lowStockThreshold = threshold
                    )
                )
                android.widget.Toast.makeText(context, "\"$name\" updated", android.widget.Toast.LENGTH_SHORT).show()
                editingItem = null
            }
        )
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Category", color = PrimaryGold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryGold, unfocusedBorderColor = BorderGold.copy(alpha = 0.5f), focusedLabelColor = PrimaryGold, unfocusedLabelColor = TextGold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isVeg, onClick = { isVeg = true }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold))
                    Text("Veg", color = TextLight)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isVeg, onClick = { isVeg = false }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold))
                    Text("Non-Veg", color = TextLight)
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, isVeg) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = DarkBrown1)) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PrimaryGold) }
        },
        containerColor = DarkBrown2
    )
}

@Composable
fun ItemDialog(
    initialItem: MenuItemEntity? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, Double, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var price by remember { mutableStateOf(initialItem?.basePrice?.toString() ?: "") }
    var foodType by remember { mutableStateOf(initialItem?.foodType ?: "veg") }
    var initialStock by remember { mutableStateOf(initialItem?.stockQuantity?.toString() ?: "0") }
    var threshold by remember { mutableStateOf(initialItem?.lowStockThreshold?.toString() ?: "10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Add New Menu Item" else "Edit Menu Item", color = PrimaryGold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryGold, unfocusedBorderColor = BorderGold.copy(alpha = 0.5f), focusedLabelColor = PrimaryGold, unfocusedLabelColor = TextGold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Base Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryGold, unfocusedBorderColor = BorderGold.copy(alpha = 0.5f), focusedLabelColor = PrimaryGold, unfocusedLabelColor = TextGold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = initialStock,
                        onValueChange = { if (it.all { c -> c.isDigit() }) initialStock = it },
                        label = { Text("Stock") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryGold, unfocusedBorderColor = BorderGold.copy(alpha = 0.5f), focusedLabelColor = PrimaryGold, unfocusedLabelColor = TextGold)
                    )
                    OutlinedTextField(
                        value = threshold,
                        onValueChange = { if (it.all { c -> c.isDigit() }) threshold = it },
                        label = { Text("Low Alert") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryGold, unfocusedBorderColor = BorderGold.copy(alpha = 0.5f), focusedLabelColor = PrimaryGold, unfocusedLabelColor = TextGold)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = foodType == "veg", onClick = { foodType = "veg" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold))
                    Text("Veg", color = TextLight)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = foodType == "non-veg", onClick = { foodType = "non-veg" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold))
                    Text("Non-Veg", color = TextLight)
                }
            }
        },
        confirmButton = {
            val p = price.toDoubleOrNull() ?: 0.0
            val s = initialStock.toIntOrNull() ?: 0
            val isEnabled = name.isNotBlank() && p > 0 && s > 0

            Button(
                enabled = isEnabled,
                onClick = { 
                    val t = threshold.toIntOrNull() ?: 10
                    onConfirm(name, p, foodType, s, t) 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGold, 
                    contentColor = DarkBrown1,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) { Text(if (initialItem == null) "Add" else "Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PrimaryGold) }
        },
        containerColor = DarkBrown2
    )
}

@Composable
fun FilterBadge(
    label: String,
    count: Int,
    backgroundColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(36.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(count.toString(), color = backgroundColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryItemRow(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color.Black.copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                category.name,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color.DarkGray
            )
            if (isSelected) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
            Switch(
                checked = category.isActive,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.6f),
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4CAF50))
            )
            Box {
                IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); expanded = false })
                }
            }
        }
        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
    }
}

@Composable
fun MenuItemRow(
    itemWithVariants: MenuWithVariants,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onManageVariants: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val item = itemWithVariants.menuItem
    val variants = itemWithVariants.variants
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Veg/Non-Veg Icon
            FoodTypeIconSmall(item.foodType)
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Text(
                    text = if (variants.isNotEmpty()) {
                        val variantText = if (variants.size == 1) "Variant" else "Variants"
                        "• ${variants.size} $variantText"
                    } else "+ Add Variants",
                    fontSize = 11.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.clickable(onClick = onManageVariants)
                )
            }

            Switch(
                checked = item.isAvailable,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.7f),
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4CAF50))
            )

            Box {
                IconButton(onClick = { expanded = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Manage Variants", color = Color(0xFF1976D2)) },
                        onClick = { onManageVariants(); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Item", color = Color.Red) },
                        onClick = { onDelete(); expanded = false }
                    )
                }
            }
        }
        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
    }
}

@Composable
fun ManageVariantsDialog(
    itemWithVariants: MenuWithVariants,
    onDismiss: () -> Unit,
    onAddVariant: (String, Double) -> Unit,
    onUpdateVariant: (ItemVariantEntity) -> Unit,
    onDeleteVariant: (ItemVariantEntity) -> Unit
) {
    val item = itemWithVariants.menuItem
    val variants = itemWithVariants.variants

    // Local add-form state
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    // In-line edit state: variantId -> (name, price text)
    var editingVariantId by remember { mutableStateOf<Int?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBrown2,
        title = {
            Column {
                Text("Manage Variants", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(item.name, color = TextGold, fontSize = 13.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Existing variants list
                if (variants.isEmpty()) {
                    Text(
                        "No variants yet. Add your first one below.",
                        color = TextGold.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    variants.forEach { variant ->
                        val isEditing = editingVariantId == variant.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEditing) DarkBrown1 else Color.Black.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isEditing) {
                                // Inline edit row
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = editName,
                                            onValueChange = { editName = it },
                                            label = { Text("Name", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = TextLight,
                                                unfocusedTextColor = TextLight,
                                                focusedBorderColor = PrimaryGold,
                                                unfocusedBorderColor = BorderGold.copy(alpha = 0.5f),
                                                focusedLabelColor = PrimaryGold,
                                                unfocusedLabelColor = TextGold
                                            )
                                        )
                                        OutlinedTextField(
                                            value = editPrice,
                                            onValueChange = { editPrice = it },
                                            label = { Text("₹ Price", fontSize = 11.sp) },
                                            modifier = Modifier.weight(0.8f),
                                            singleLine = true,
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = TextLight,
                                                unfocusedTextColor = TextLight,
                                                focusedBorderColor = PrimaryGold,
                                                unfocusedBorderColor = BorderGold.copy(alpha = 0.5f),
                                                focusedLabelColor = PrimaryGold,
                                                unfocusedLabelColor = TextGold
                                            )
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { editingVariantId = null }) {
                                            Text("Cancel", color = TextGold, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val isPriceValid = editPrice.toDoubleOrNull() != null && editPrice.toDouble() > 0
                                        Button(
                                            onClick = {
                                                if (editName.isNotBlank() && isPriceValid) {
                                                    onUpdateVariant(variant.copy(variantName = editName.trim(), price = editPrice.toDouble()))
                                                    editingVariantId = null
                                                }
                                            },
                                            enabled = editName.isNotBlank() && isPriceValid,
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = DarkBrown1),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            } else {
                                // Display row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(variant.variantName, color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("₹ ${"%,.0f".format(variant.price)}", color = PrimaryGold, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            editingVariantId = variant.id
                                            editName = variant.variantName
                                            editPrice = variant.price.toString()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = PrimaryGold, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteVariant(variant) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderGold.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Add new variant row
                Text("Add New Variant", color = PrimaryGold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("e.g. Half, Full", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = BorderGold.copy(alpha = 0.5f),
                            focusedLabelColor = PrimaryGold,
                            unfocusedLabelColor = TextGold
                        )
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("₹ Price", fontSize = 11.sp) },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = BorderGold.copy(alpha = 0.5f),
                            focusedLabelColor = PrimaryGold,
                            unfocusedLabelColor = TextGold
                        )
                    )
                    val isAddEnabled = newName.isNotBlank() && (newPrice.toDoubleOrNull() ?: 0.0) > 0
                    IconButton(
                        onClick = {
                            if (isAddEnabled) {
                                onAddVariant(newName.trim(), newPrice.toDouble())
                                newName = ""
                                newPrice = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isAddEnabled) PrimaryGold else Color.Gray.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(Icons.Default.Add, null, tint = DarkBrown1, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = DarkBrown1)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = null
    )
}

@Composable
fun FoodTypeIconSmall(type: String) {
    val color = if (type == "veg") VegGreen else NonVegRed
    Box(
        modifier = Modifier
            .size(12.dp)
            .border(1.dp, color)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color, RoundedCornerShape(100.dp))
        )
    }
}

