package com.khanabook.lite.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.khanabook.lite.pos.data.local.entity.UserEntity
import com.khanabook.lite.pos.domain.util.*
import com.khanabook.lite.pos.ui.theme.*
import com.khanabook.lite.pos.ui.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    onBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.allUsers.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showSidebar by remember { mutableStateOf(true) }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryGold,
                contentColor = DarkBrown1,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Staff", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(12.dp)
            )
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
                        text = "Staff Management",
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
                    // Staff List (Left Column) - Conditional
                    if (showSidebar) {
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "Staff Members",
                                color = PrimaryGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider(color = BorderGold.copy(alpha = 0.2f))

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(users) { user ->
                                    Surface(
                                        onClick = { selectedUser = user },
                                        color = if (selectedUser?.id == user.id) PrimaryGold.copy(alpha = 0.15f) else Color.Transparent,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                user.name,
                                                color = if (selectedUser?.id == user.id) PrimaryGold else TextLight,
                                                fontSize = 14.sp,
                                                fontWeight = if (selectedUser?.id == user.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                user.role.uppercase(),
                                                color = if (selectedUser?.id == user.id) TextGold else TextGold.copy(alpha = 0.6f),
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

                    // Details View (Right Column)
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                        if (selectedUser != null) {
                            val user = selectedUser!!
                            Column {
                                Text(
                                    "Staff Details",
                                    color = TextGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    user.name,
                                    color = PrimaryGold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = BorderGold.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                DetailItem("Phone", user.whatsappNumber ?: user.email)
                                DetailItem("Role", user.role.uppercase())
                                DetailItem("Status", if (user.isActive) "Active" else "Inactive")
                                DetailItem("Joined", user.createdAt.split(" ")[0])
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                if (user.role != "admin") {
                                    Button(
                                        onClick = { 
                                            viewModel.deleteUser(user)
                                            selectedUser = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.1f), contentColor = DangerRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                                    ) {
                                        Icon(Icons.Default.Delete, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Remove Staff Member")
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Badge, null, tint = TextGold.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Select a staff member from the ${if(showSidebar) "left" else "sidebar"}\nto view details or manage access", 
                                        color = TextGold, 
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                    if (!showSidebar) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { showSidebar = true }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)) {
                                            Text("Show Staff List", color = DarkBrown1)
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

    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, role, pass ->
                viewModel.addUser(name, phone, role, pass)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGold, fontSize = 14.sp)
        Text(value, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("staff") }
    var password by remember { mutableStateOf("") }

    // Validation
    val isNameValid = isValidName(name)
    val isPhoneValid = isValidPhone(phone)
    val isPasswordValid = password.length >= 4 // Shorter PINs allowed for staff
    val isAddEnabled = isNameValid && isPhoneValid && isPasswordValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Staff", color = PrimaryGold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Full Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isNotEmpty() && !isNameValid,
                    supportingText = { if (name.isNotEmpty() && !isNameValid) Text("Too short", color = DangerRed) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { if (it.length <= 10) phone = it }, 
                    label = { Text("Phone Number") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = phone.isNotEmpty() && !isPhoneValid,
                    supportingText = { if (phone.isNotEmpty() && !isPhoneValid) Text("Invalid 10-digit number", color = DangerRed) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("PIN / Password") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = password.isNotEmpty() && !isPasswordValid,
                    supportingText = { if (password.isNotEmpty() && !isPasswordValid) Text("Min 4 characters", color = DangerRed) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == "staff", onClick = { role = "staff" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold))
                    Text("Staff", color = TextLight); Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = role == "admin", onClick = { role = "admin" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold))
                    Text("Admin", color = TextLight)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isAddEnabled) onConfirm(name, phone, role, password) },
                enabled = isAddEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = DarkBrown1)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextGold) } },
        containerColor = DarkBrown2
    )
}
