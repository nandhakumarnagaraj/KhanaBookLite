package com.khanabook.lite.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialStockLogEntity
import com.khanabook.lite.pos.ui.theme.*
import com.khanabook.lite.pos.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val materials by viewModel.rawMaterials.collectAsState()
    val logs by viewModel.stockLogs.collectAsState()
    val selectedId by viewModel.selectedMaterialId.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf<RawMaterialEntity?>(null) }
    var showThresholdDialog by remember { mutableStateOf<RawMaterialEntity?>(null) }
    var showLogsTab by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    text = "Raw Materials Inventory",
                    modifier = Modifier.weight(1f),
                    color = PrimaryGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Material", tint = PrimaryGold)
                }
            }

            // Tab Toggle
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Surface(
                    onClick = { 
                        showLogsTab = false
                        viewModel.selectMaterial(null) 
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    color = if (!showLogsTab) PrimaryGold else Color.Transparent,
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    border = if (!showLogsTab) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Materials", color = if (!showLogsTab) DarkBrown1 else TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    onClick = { 
                        showLogsTab = true
                        viewModel.selectMaterial(null) 
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    color = if (showLogsTab) PrimaryGold else Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    border = if (showLogsTab) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Stock Logs", color = if (showLogsTab) DarkBrown1 else TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showLogsTab && selectedId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val matName = materials.find { it.id == selectedId }?.name ?: "Unknown"
                    Text("History: $matName", color = PrimaryGold, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.selectMaterial(null) }) {
                        Text("Show All Logs", color = TextGold, fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                if (!showLogsTab) {
                    items(materials) { material ->
                        RawMaterialItemCard(
                            material = material,
                            onAdjust = { showAdjustDialog = material },
                            onThreshold = { showThresholdDialog = material },
                            onDelete = { viewModel.deleteMaterial(material) },
                            onShowLogs = {
                                viewModel.selectMaterial(material.id)
                                showLogsTab = true
                            }
                        )
                    }
                } else {
                    if (logs.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No logs found", color = TextGold)
                            }
                        }
                    }
                    items(logs) { log ->
                        val matName = materials.find { it.id == log.rawMaterialId }?.name ?: "Unknown"
                        val unit = materials.find { it.id == log.rawMaterialId }?.unit ?: ""
                        RawMaterialStockLogCard(log, matName, unit)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMaterialDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, unit, stock, threshold ->
                viewModel.addRawMaterial(name, unit, stock, threshold)
                showAddDialog = false
            }
        )
    }

    showAdjustDialog?.let { material ->
        RawMaterialAdjustDialog(material, onDismiss = { showAdjustDialog = null }, onConfirm = { delta, reason ->
            viewModel.adjustStock(material.id, delta, reason); showAdjustDialog = null
        })
    }

    showThresholdDialog?.let { material ->
        RawMaterialThresholdDialog(material, onDismiss = { showThresholdDialog = null }, onConfirm = { threshold ->
            viewModel.updateThreshold(material.id, threshold); showThresholdDialog = null
        })
    }
}

@Composable
fun RawMaterialItemCard(
    material: RawMaterialEntity, 
    onAdjust: () -> Unit, 
    onThreshold: () -> Unit, 
    onDelete: () -> Unit,
    onShowLogs: () -> Unit
) {
    val isOut = material.currentStock <= 0
    val isLow = !isOut && material.currentStock <= material.lowStockThreshold
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onShowLogs() },
        colors = CardDefaults.cardColors(containerColor = DarkBrown2),
        border = if (isOut) androidx.compose.foundation.BorderStroke(1.dp, DangerRed) 
                 else if (isLow) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)) 
                 else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(material.name, color = TextLight, fontWeight = FontWeight.Bold)
                    Text("Threshold: ${material.lowStockThreshold} ${material.unit}", color = TextGold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${material.currentStock} ${material.unit}", 
                        color = if (isOut) DangerRed else if (isLow) Color(0xFFFF9800) else VegGreen, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (isOut) Text("OUT OF STOCK", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    else if (isLow) Text("RUNNING LOW", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdjust, modifier = Modifier.weight(1f).height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)) {
                    Icon(Icons.Default.Edit, null, tint = DarkBrown1, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Stock", color = DarkBrown1, fontSize = 12.sp)
                }
                OutlinedButton(onClick = onThreshold, modifier = Modifier.weight(1f).height(36.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold)) {
                    Icon(Icons.Default.Settings, null, tint = TextGold, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Limit", color = TextGold, fontSize = 12.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, null, tint = DangerRed.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun RawMaterialStockLogCard(log: RawMaterialStockLogEntity, matName: String, unit: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (log.delta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = if (log.delta > 0) VegGreen else DangerRed)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(matName, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${log.reason} | ${log.createdAt}", color = TextGold, fontSize = 11.sp)
            }
            Text("${if (log.delta > 0) "+" else ""}${log.delta} $unit", color = if (log.delta > 0) VegGreen else DangerRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddMaterialDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var stock by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("5.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Raw Material", color = PrimaryGold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Material Name (e.g. Flour)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (kg, ltr, pcs)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text("Low Stock Alert Limit") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { 
            Button(onClick = { 
                onConfirm(name, unit, stock.toDoubleOrNull() ?: 0.0, threshold.toDoubleOrNull() ?: 5.0) 
            }, enabled = name.isNotBlank()) { Text("Add") } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}

@Composable
fun RawMaterialAdjustDialog(material: RawMaterialEntity, onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Purchase") }
    var isAdd by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${material.name}", color = PrimaryGold) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = isAdd, onClick = { isAdd = true }, label = { Text("Restock (+)") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = !isAdd, onClick = { isAdd = false }, label = { Text("Consume (-)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Quantity (${material.unit})") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onConfirm(if (isAdd) amount.toDoubleOrNull() ?: 0.0 else -(amount.toDoubleOrNull() ?: 0.0), reason) }) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}

@Composable
fun RawMaterialThresholdDialog(material: RawMaterialEntity, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var threshold by remember { mutableStateOf(material.lowStockThreshold.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Low Stock Alert Limit", color = PrimaryGold) },
        text = {
            Column {
                Text("Set alert threshold for ${material.name}", color = TextLight, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text("Alert when stock below (${material.unit})") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onConfirm(threshold.toDoubleOrNull() ?: 5.0) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}
