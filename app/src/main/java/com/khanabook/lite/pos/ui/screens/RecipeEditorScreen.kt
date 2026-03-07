package com.khanabook.lite.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.khanabook.lite.pos.data.local.entity.MenuItemEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.local.entity.RecipeIngredientEntity
import com.khanabook.lite.pos.ui.theme.*
import com.khanabook.lite.pos.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    onBack: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val items by viewModel.menuItems.collectAsState()
    val rawMaterials by viewModel.rawMaterials.collectAsState()
    val selectedItemId by viewModel.selectedMenuItemId.collectAsState()
    val currentIngredients by viewModel.currentRecipeIngredients.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<RecipeIngredientEntity?>(null) }
    var showSidebar by remember { mutableStateOf(true) }

    Scaffold(
        floatingActionButton = {
            if (selectedItemId != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryGold,
                    contentColor = DarkBrown1,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Ingredient", fontWeight = FontWeight.Bold) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = DarkBrown1
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(listOf(DarkBrown1, DarkBrown2)))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PrimaryGold)
                    }
                    Text(
                        text = "Recipe & BOM Editor",
                        modifier = Modifier.weight(1f),
                        color = PrimaryGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { showSidebar = !showSidebar }) {
                        Icon(
                            if (showSidebar) Icons.Default.Fullscreen else Icons.AutoMirrored.Filled.ViewSidebar, 
                            contentDescription = "Toggle Sidebar", 
                            tint = PrimaryGold
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    // Menu Items List (Left Column) - Conditional
                    if (showSidebar) {
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "Menu Items",
                                color = PrimaryGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider(color = BorderGold.copy(alpha = 0.2f))

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(items) { item ->
                                    Surface(
                                        onClick = { viewModel.selectMenuItem(item.id) },
                                        color = if (selectedItemId == item.id) PrimaryGold.copy(alpha = 0.15f) else Color.Transparent,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (selectedItemId == item.id) PrimaryGold else Color.Transparent,
                                                        androidx.compose.foundation.shape.CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                item.name,
                                                color = if (selectedItemId == item.id) PrimaryGold else TextLight,
                                                fontSize = 14.sp,
                                                fontWeight = if (selectedItemId == item.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = BorderGold.copy(alpha = 0.1f), thickness = 0.5.dp)
                                }
                            }
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(BorderGold.copy(alpha = 0.2f)))
                    }

                    // Ingredients View (Right Column) - Expands to full width if sidebar is hidden
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                        if (selectedItemId != null) {
                            val selectedItem = items.find { it.id == selectedItemId }

                            Column {
                                Text(
                                    "Recipe Configuration",
                                    color = TextGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    selectedItem?.name ?: "",
                                    color = PrimaryGold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = BorderGold.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            if (currentIngredients.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Inventory, null, tint = TextGold.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No ingredients added yet", color = TextGold.copy(alpha = 0.5f), fontSize = 14.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp) // Room for FAB
                                ) {
                                    items(currentIngredients) { ingredient ->
                                        val material = rawMaterials.find { it.id == ingredient.rawMaterialId }
                                        IngredientCard(
                                            material = material,
                                            quantity = ingredient.quantityNeeded,
                                            onEdit = { showEditDialog = ingredient },
                                            onDelete = { viewModel.removeIngredient(ingredient) }
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.RestaurantMenu, null, tint = TextGold.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Select a menu item from the ${if(showSidebar) "left" else "sidebar"}\nto manage its recipe ingredients", 
                                        color = TextGold, 
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                    if (!showSidebar) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { showSidebar = true }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)) {
                                            Text("Show Menu Items", color = DarkBrown1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && selectedItemId != null) {
        AddIngredientDialog(
            rawMaterials = rawMaterials,
            onDismiss = { showAddDialog = false },
            onConfirm = { materialId, qty ->
                viewModel.addIngredient(selectedItemId!!, materialId, qty)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { ingredient ->
        val material = rawMaterials.find { it.id == ingredient.rawMaterialId }
        EditIngredientDialog(
            materialName = material?.name ?: "Unknown",
            unit = material?.unit ?: "",
            currentQuantity = ingredient.quantityNeeded,
            onDismiss = { showEditDialog = null },
            onConfirm = { newQty ->
                viewModel.updateIngredient(ingredient, newQty)
                showEditDialog = null
            }
        )
    }
}

@Composable
fun IngredientCard(
    material: RawMaterialEntity?, 
    quantity: Double, 
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(material?.name ?: "Unknown", color = TextLight, fontWeight = FontWeight.Bold)
                Text("${quantity} ${material?.unit ?: ""}", color = TextGold, fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = TextGold.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddIngredientDialog(
    rawMaterials: List<RawMaterialEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    var selectedMaterialId by remember { mutableStateOf<Int?>(null) }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient", color = PrimaryGold) },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = rawMaterials.find { it.id == selectedMaterialId }?.name ?: "Select Material",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = !expanded }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        rawMaterials.forEach { mat ->
                            DropdownMenuItem(
                                text = { Text(mat.name) },
                                onClick = { selectedMaterialId = mat.id; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity Needed") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedMaterialId != null) onConfirm(selectedMaterialId!!, quantity.toDoubleOrNull() ?: 0.0) },
                enabled = selectedMaterialId != null && quantity.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}

@Composable
fun EditIngredientDialog(
    materialName: String,
    unit: String,
    currentQuantity: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantity by remember { mutableStateOf(currentQuantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit: $materialName", color = PrimaryGold) },
        text = {
            Column {
                Text("Update quantity needed for this recipe.", color = TextLight, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity Needed ($unit)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity.toDoubleOrNull() ?: currentQuantity) },
                enabled = quantity.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = DarkBrown1)
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextGold) } },
        containerColor = DarkBrown2
    )
}
