package com.khanabook.lite.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.khanabook.lite.pos.data.local.entity.MaterialBatchEntity
import com.khanabook.lite.pos.data.local.entity.RawMaterialEntity
import com.khanabook.lite.pos.ui.theme.*
import com.khanabook.lite.pos.ui.viewmodel.BatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchManagementScreen(
    onBack: () -> Unit,
    viewModel: BatchViewModel = hiltViewModel()
) {
    val materials by viewModel.rawMaterials.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val selectedId by viewModel.selectedMaterialId.collectAsState()
    val alerts by viewModel.expiringSoon.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSidebar by remember { mutableStateOf(true) }

    Scaffold(
        floatingActionButton = {
            if (selectedId != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryGold,
                    contentColor = DarkBrown1,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Batch", fontWeight = FontWeight.Bold) },
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
                        text = "Batch & Expiry",
                        modifier = Modifier.weight(1f),
                        color = PrimaryGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { showSidebar = !showSidebar }) {
                        Icon(
                            if (showSidebar) Icons.Default.Fullscreen else Icons.Default.ViewSidebar, 
                            contentDescription = "Toggle Sidebar", 
                            tint = PrimaryGold
                        )
                    }
                }

                // Dashboard Alerts
                if (alerts.isNotEmpty() && selectedId == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = DangerRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${alerts.size} batches expiring within 7 days!", 
                                color = TextLight, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    // Materials List (Left Column) - Conditional
                    if (showSidebar) {
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "Raw Materials",
                                color = PrimaryGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider(color = BorderGold.copy(alpha = 0.2f))

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(materials) { mat ->
                                    Surface(
                                        onClick = { viewModel.selectMaterial(mat.id) },
                                        color = if (selectedId == mat.id) PrimaryGold.copy(alpha = 0.15f) else Color.Transparent,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                mat.name,
                                                color = if (selectedId == mat.id) PrimaryGold else TextLight,
                                                fontSize = 14.sp,
                                                fontWeight = if (selectedId == mat.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                "Stock: ${mat.currentStock} ${mat.unit}",
                                                color = if (selectedId == mat.id) TextGold else TextGold.copy(alpha = 0.6f),
                                                fontSize = 11.sp
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

                    // Batch Details (Right Column) - Expands to full width if sidebar is hidden
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                        if (selectedId != null) {
                            val material = materials.find { it.id == selectedId }
                            
                            Column {
                                Text(
                                    "Batch Inventory",
                                    color = TextGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    material?.name ?: "",
                                    color = PrimaryGold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = BorderGold.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            if (batches.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Inventory, null, tint = TextGold.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No active batches found", color = TextGold.copy(alpha = 0.5f), fontSize = 14.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp) // Room for FAB
                                ) {
                                    items(batches) { batch ->
                                        BatchCard(batch, material?.unit ?: "")
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = TextGold.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Select a material from the ${if(showSidebar) "left" else "sidebar"}\nto manage its batches and expiry", 
                                        color = TextGold, 
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                    if (!showSidebar) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { showSidebar = true }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)) {
                                            Text("Show Raw Materials", color = DarkBrown1)
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

    if (showAddDialog && selectedId != null) {
        AddBatchDialog(
            materialName = materials.find { it.id == selectedId }?.name ?: "",
            unit = materials.find { it.id == selectedId }?.unit ?: "",
            onDismiss = { showAddDialog = false },
            onConfirm = { qty, expiry, batchNo ->
                viewModel.addBatch(selectedId!!, qty, expiry, batchNo)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BatchCard(batch: MaterialBatchEntity, unit: String) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isExpired = batch.expiryDate <= today
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isExpired) DangerRed.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Qty: ${batch.quantity} / ${batch.initialQuantity} $unit", 
                    color = TextLight, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (isExpired) {
                    Text("EXPIRED", color = DangerRed, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                }
            }
            Text("Batch #: ${batch.batchNumber ?: "N/A"}", color = TextGold, fontSize = 11.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Received: ${batch.receivedDate.split(" ")[0]}", color = TextGold, fontSize = 11.sp)
                Text("Expires: ${batch.expiryDate}", color = if (isExpired) DangerRed else VegGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            // Progress Bar for Batch Consumption
            val progress = (batch.quantity / batch.initialQuantity).toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp),
                color = if (isExpired) DangerRed else PrimaryGold,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun AddBatchDialog(
    materialName: String,
    unit: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String?) -> Unit
) {
    var qty by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var batchNo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Batch: $materialName", color = PrimaryGold) },
        text = {
            Column {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantity Received ($unit)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { expiry = it },
                    label = { Text("Expiry Date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2026-12-31") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = batchNo,
                    onValueChange = { batchNo = it },
                    label = { Text("Batch Number (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(qty.toDoubleOrNull() ?: 0.0, expiry, batchNo.ifBlank { null }) },
                enabled = qty.isNotEmpty() && expiry.length >= 10
            ) { Text("Add Batch") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = DarkBrown2
    )
}
