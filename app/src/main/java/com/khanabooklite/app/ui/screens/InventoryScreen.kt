package com.khanabooklite.app.ui.screens

import androidx.compose.foundation.background
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
import com.khanabooklite.app.data.local.entity.MenuItemEntity
import com.khanabooklite.app.data.local.entity.StockLogEntity
import com.khanabooklite.app.ui.theme.*
import com.khanabooklite.app.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val items by viewModel.menuItems.collectAsState()
    val logs by viewModel.stockLogs.collectAsState()
    val selectedId by viewModel.selectedItemId.collectAsState()
    
    var showAdjustDialog by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showThresholdDialog by remember { mutableStateOf<MenuItemEntity?>(null) }

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
                    text = "Inventory Management",
                    modifier = Modifier.weight(1f),
                    color = PrimaryGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                // Empty spacer to balance the back button
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Tab Toggle
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Surface(
                    onClick = { viewModel.selectItem(null) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    color = if (selectedId == null) PrimaryGold else Color.Transparent,
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    border = if (selectedId == null) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("All Items", color = if (selectedId == null) DarkBrown1 else TextLight, fontSize = 14.sp)
                    }
                }
                Surface(
                    onClick = { /* History */ },
                    modifier = Modifier.weight(1f).height(40.dp),
                    color = if (selectedId != null) PrimaryGold else Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    border = if (selectedId != null) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Stock Logs", color = if (selectedId != null) DarkBrown1 else TextLight, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                if (selectedId == null) {
                    items(items) { item ->
                        InventoryItemCard(
                            item = item,
                            onAdjust = { showAdjustDialog = item },
                            onThreshold = { showThresholdDialog = item }
                        )
                    }
                } else {
                    items(logs) { log ->
                        StockLogCard(log, items.find { it.id == log.menuItemId }?.name ?: "Unknown")
                    }
                }
            }
        }
    }

    showAdjustDialog?.let { item ->
        StockAdjustDialog(item, onDismiss = { showAdjustDialog = null }, onConfirm = { delta, reason ->
            viewModel.adjustStock(item.id, delta, reason); showAdjustDialog = null
        })
    }

    showThresholdDialog?.let { item ->
        ThresholdDialog(item, onDismiss = { showThresholdDialog = null }, onConfirm = { threshold ->
            viewModel.updateThreshold(item.id, threshold); showThresholdDialog = null
        })
    }
}

@Composable
fun InventoryItemCard(item: MenuItemEntity, onAdjust: () -> Unit, onThreshold: () -> Unit) {
    val isLow = item.stockQuantity <= item.lowStockThreshold
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBrown2),
        border = if (isLow) androidx.compose.foundation.BorderStroke(1.dp, DangerRed) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(item.name, color = TextLight, fontWeight = FontWeight.Bold)
                    Text("Threshold: ${item.lowStockThreshold}", color = TextGold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${item.stockQuantity}", color = if (isLow) DangerRed else VegGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    if (isLow) Text("LOW STOCK", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdjust, modifier = Modifier.weight(1f).height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)) {
                    Icon(Icons.Default.Add, null, tint = DarkBrown1, modifier = Modifier.size(16.dp)); Text("Adjust", color = DarkBrown1, fontSize = 12.sp)
                }
                OutlinedButton(onClick = onThreshold, modifier = Modifier.weight(1f).height(36.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold)) {
                    Icon(Icons.Default.Settings, null, tint = TextGold, modifier = Modifier.size(16.dp)); Text("Limit", color = TextGold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StockLogCard(log: StockLogEntity, itemName: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (log.delta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = if (log.delta > 0) VegGreen else DangerRed)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(itemName, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${log.reason} | ${log.createdAt}", color = TextGold, fontSize = 11.sp)
            }
            Text("${if (log.delta > 0) "+" else ""}${log.delta}", color = if (log.delta > 0) VegGreen else DangerRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StockAdjustDialog(item: MenuItemEntity, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Manual Adjustment") }
    var isAdd by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${item.name}", color = PrimaryGold) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = isAdd, onClick = { isAdd = true }, label = { Text("Add (+)") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = !isAdd, onClick = { isAdd = false }, label = { Text("Subtract (-)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = amount, onValueChange = { if (it.all { it.isDigit() }) amount = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onConfirm(if (isAdd) amount.toIntOrNull() ?: 0 else -(amount.toIntOrNull() ?: 0), reason) }) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}

@Composable
fun ThresholdDialog(item: MenuItemEntity, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var threshold by remember { mutableStateOf(item.lowStockThreshold.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Low Stock Alert Limit", color = PrimaryGold) },
        text = {
            Column {
                Text("Set alert threshold for ${item.name}", color = TextLight, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = threshold, onValueChange = { if (it.all { it.isDigit() }) threshold = it }, label = { Text("Alert when stock below") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onConfirm(threshold.toIntOrNull() ?: 10) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}
