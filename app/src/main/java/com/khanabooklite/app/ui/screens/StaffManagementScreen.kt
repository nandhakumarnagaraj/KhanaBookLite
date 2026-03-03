package com.khanabooklite.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.khanabooklite.app.data.local.entity.UserEntity
import com.khanabooklite.app.domain.util.*
import com.khanabooklite.app.ui.theme.*
import com.khanabooklite.app.ui.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    onBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.allUsers.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBrown1, Color.Black)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryGold)
                }
                Text("Manage Staff", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, null, tint = PrimaryGold, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    StaffUserCard(user, onDelete = { viewModel.deleteUser(user) })
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
fun StaffUserCard(user: UserEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = PrimaryGold.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Role: ${user.role.uppercase()} | ${user.whatsappNumber ?: user.email}",
                    color = TextGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (user.role != "admin") {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = DangerRed.copy(alpha = 0.8f))
                }
            }
        }
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
