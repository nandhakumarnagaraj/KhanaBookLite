package com.khanabook.lite.pos.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.khanabook.lite.pos.data.local.entity.*
import com.khanabook.lite.pos.domain.manager.BluetoothPrinterManager
import com.khanabook.lite.pos.domain.util.*
import com.khanabook.lite.pos.ui.theme.*
import com.khanabook.lite.pos.ui.viewmodel.AuthViewModel
import com.khanabook.lite.pos.ui.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        onBack: () -> Unit,
        viewModel: SettingsViewModel = hiltViewModel(),
        authViewModel: AuthViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    var section by remember { mutableStateOf("menu") }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(listOf(DarkBrown1, DarkBrown2)))
                            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Header
            if (section != "menu_config" && section != "staff_mgmt") {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                            onClick = { if (section == "menu") onBack() else section = "menu" }
                    ) {
                        Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryGold
                        )
                    }
                    Text(
                            text =
                                    when (section) {
                                        "shop" -> "Shop Configuration"
                                        "payment" -> "Payment Configuration"
                                        "printer" -> "Printer Configuration"
                                        "tax" -> "Tax Configuration"
                                        "preview" -> "Invoice Preview"
                                        "backup" -> "Data Backup"
                                        "menu" -> "Settings"
                                        else -> "Settings"
                                    },
                            modifier = Modifier.weight(1f),
                            color = PrimaryGold,
                            fontSize = if (section == "menu") 24.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                    )
                    // Empty spacer to balance the back button
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (section) {
                    "menu" -> {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsItem(
                                    icon = Icons.Filled.Store,
                                    text = "Shop/Restaurant Configuration"
                            ) { section = "shop" }
                            SettingsItem(
                                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                    text = "Menu Configuration"
                            ) { section = "menu_config" }
                            SettingsItem(
                                    icon = Icons.Filled.CreditCard,
                                    text = "Payment Configuration"
                            ) { section = "payment" }
                            SettingsItem(
                                    icon = Icons.Filled.Print,
                                    text = "Printer Configuration"
                            ) { section = "printer" }
                            SettingsItem(icon = Icons.Filled.Settings, text = "Tax Configuration") {
                                section = "tax"
                            }
                            SettingsItem(icon = Icons.Filled.Visibility, text = "Preview Invoice") {
                                section = "preview"
                            }
                            SettingsItem(icon = Icons.Filled.Backup, text = "Data Backup") {
                                section = "backup"
                            }
                            SettingsItem(icon = Icons.Filled.People, text = "Manage Staff") {
                                section = "staff_mgmt"
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardBG),
                                    border =
                                            AssistChipDefaults.assistChipBorder(
                                                    enabled = true,
                                                    borderColor = BorderGold
                                            )
                            ) {
                                val ctx = LocalContext.current
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                            "Logout from Account",
                                            color = TextLight,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                    )
                                    Button(
                                            onClick = {
                                                authViewModel.logout()
                                                Toast.makeText(
                                                                ctx,
                                                                "Logged out successfully",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            },
                                            colors =
                                                    ButtonDefaults.buttonColors(
                                                            containerColor = DangerRed
                                                    ),
                                            shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                                Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Logout", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    "shop" -> {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            ShopConfigView(profile, viewModel, authViewModel) { section = "menu" }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    "menu_config" -> {
                        MenuConfigurationScreen(onBack = { section = "menu" })
                    }
                    "payment" -> {
                        val ctx = LocalContext.current
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            PaymentConfigView(
                                    profile = profile,
                                    onSave = {
                                        viewModel.saveProfile(it)
                                        Toast.makeText(
                                                        ctx,
                                                        "Payment settings saved",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        section = "menu"
                                    },
                                    onBack = { section = "menu" }
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    "printer" -> {
                        val ctx = LocalContext.current
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            PrinterConfigView(
                                    profile = profile,
                                    onSave = {
                                        viewModel.saveProfile(it)
                                        Toast.makeText(
                                                        ctx,
                                                        "Printer settings saved",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        section = "menu"
                                    },
                                    onBack = { section = "menu" },
                                    viewModel = viewModel
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    "tax" -> {
                        val ctx = LocalContext.current
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            TaxConfigView(
                                    profile = profile,
                                    onSave = {
                                        viewModel.saveProfile(it)
                                        Toast.makeText(
                                                        ctx,
                                                        "Tax settings saved",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        section = "menu"
                                    },
                                    onBack = { section = "menu" }
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    "preview" -> {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            PreviewInvoiceView(profile = profile, onBack = { section = "menu" })
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    "backup" -> {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                            BackupConfigView(viewModel = viewModel, onBack = { section = "menu" })
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    "staff_mgmt" -> {
                        StaffManagementScreen(onBack = { section = "menu" })
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBG),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Column(modifier = Modifier.padding(20.dp)) { content() } }
}

@Composable
fun BackupConfigView(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val restoreLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    viewModel.restoreDatabase(context, it) { success ->
                        android.widget.Toast.makeText(
                                        context,
                                        if (success) "Restore successful! Please restart the app."
                                        else "Restore failed",
                                        android.widget.Toast.LENGTH_LONG
                                )
                                .show()
                    }
                }
            }

    ConfigCard {
        Icon(
                Icons.Default.CloudUpload,
                null,
                tint = Color(0xFF5D4037),
                modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Export Database", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
                "Save a backup of your menu, bills, and settings to your device storage.",
                color = TextGold,
                fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                    onClick = {
                        viewModel.backupDatabase(context) { success ->
                            android.widget.Toast.makeText(
                                            context,
                                            if (success) "Backup created" else "Backup failed",
                                            android.widget.Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
            ) { Text("BACKUP NOW", color = Color.White, fontWeight = FontWeight.Bold) }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
        }
    }

    ConfigCard {
        Icon(
                Icons.Default.CloudDownload,
                null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
                "Restore Database",
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
        )
        Text(
                "Replace current data with a previously saved backup file (.db).",
                color = TextGold,
                fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(
                onClick = { restoreLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
        ) { Text("RESTORE FROM FILE", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun PreviewInvoiceView(profile: RestaurantProfileEntity?, onBack: () -> Unit) {
    var previewPaperSize by remember { mutableStateOf(profile?.paperSize ?: "58mm") }
    var simulateGst by remember { mutableStateOf(profile?.gstEnabled ?: false) }

    ConfigCard {
        Text("Preview Options", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paper Size", color = TextGold, fontSize = 14.sp)
            Row {
                FilterChip(
                        selected = previewPaperSize == "58mm",
                        onClick = { previewPaperSize = "58mm" },
                        label = { Text("58mm") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                        selected = previewPaperSize == "80mm",
                        onClick = { previewPaperSize = "80mm" },
                        label = { Text("80mm") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Simulate GST", color = TextGold, fontSize = 14.sp)
            Switch(
                    checked = simulateGst,
                    onCheckedChange = { simulateGst = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextGold),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold)
        ) { Text("CLOSE PREVIEW") }
    }

    val previewWidth = if (previewPaperSize == "80mm") 300.dp else 220.dp
    Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
    ) {
        Card(
                modifier = Modifier.width(previewWidth),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        profile?.shopName?.uppercase() ?: "RESTAURANT",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (previewPaperSize == "80mm") 16.sp else 13.sp
                )
                Text(
                        profile?.shopAddress?.take(30) ?: "Address Line",
                        color = Color.Gray,
                        fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Text(
                        if (simulateGst) "TAX INVOICE" else "INVOICE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                )
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bill: #00001", color = Color.Black, fontSize = 8.sp)
                    Text("28-Feb-2026", color = Color.Black, fontSize = 8.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color.Black, thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(
                            "ITEM",
                            modifier = Modifier.weight(1f),
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            "QTY",
                            modifier = Modifier.width(25.dp),
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            "AMT",
                            modifier = Modifier.width(40.dp),
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Right
                    )
                }
                HorizontalDivider(color = Color.Black, thickness = 0.5.dp)

                MockInvoiceItem("CHICKEN BIRYANI", 1, 320.0)
                MockInvoiceItem("LIME SODA", 2, 140.0)

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                val subtotal = 460.0
                val currency = if (profile?.currency == "INR") "₹" else profile?.currency ?: ""

                InvoiceSummaryRow(
                        "Subtotal",
                        "$currency ${"%.2f".format(subtotal)}",
                        previewPaperSize
                )
                if (simulateGst) {
                    val gstPct = profile?.gstPercentage ?: 5.0
                    val totalGst = (subtotal * gstPct) / 100
                    InvoiceSummaryRow(
                            "GST ($gstPct%)",
                            "$currency ${"%.2f".format(totalGst)}",
                            previewPaperSize
                    )
                }

                HorizontalDivider(
                        color = Color.Black,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 2.dp)
                )
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                            "NET AMOUNT",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                    )
                    val total =
                            if (simulateGst)
                                    subtotal + (subtotal * (profile?.gstPercentage ?: 5.0) / 100)
                            else subtotal
                    Text(
                            "$currency ${"%.2f".format(total)}",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        "VISIT AGAIN",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MockInvoiceItem(name: String, qty: Int, total: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(name, modifier = Modifier.weight(1f), color = Color.Black, fontSize = 8.sp)
        Text(qty.toString(), modifier = Modifier.width(25.dp), color = Color.Black, fontSize = 8.sp)
        Text(
                "%.2f".format(total),
                modifier = Modifier.width(40.dp),
                color = Color.Black,
                fontSize = 8.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Right
        )
    }
}

@Composable
fun InvoiceSummaryRow(label: String, value: String, previewSize: String) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
                label,
                color = Color.Black,
                fontSize = 8.sp,
                modifier = Modifier.width(if (previewSize == "80mm") 80.dp else 60.dp)
        )
        Text(
                value,
                color = Color.Black,
                fontSize = 8.sp,
                modifier = Modifier.width(50.dp),
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Right
        )
    }
}

@Composable
private fun ShopConfigView(
        profile: RestaurantProfileEntity?,
        viewModel: SettingsViewModel,
        authViewModel: AuthViewModel,
        onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(profile?.shopName ?: "") }
    var address by remember { mutableStateOf(profile?.shopAddress ?: "") }
    var whatsapp by remember { mutableStateOf(profile?.whatsappNumber ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var logoPath by remember { mutableStateOf(profile?.logoPath) }
    var consent by remember { mutableStateOf(profile?.emailInvoiceConsent ?: false) }

    val isNameValid = isValidName(name)
    val isAddressValid = address.isNotBlank()
    val isEmailValid = isValidEmail(email)

    var isOtpSent by remember { mutableStateOf(false) }
    var otpValue by remember { mutableStateOf("") }
    var isVerified by remember {
        mutableStateOf(whatsapp == profile?.whatsappNumber && whatsapp.isNotEmpty())
    }
    var otpTimer by remember { mutableIntStateOf(0) }

    val signUpStatus by authViewModel.signUpStatus.collectAsState()

    LaunchedEffect(signUpStatus) {
        if (signUpStatus is AuthViewModel.SignUpResult.OtpSent) {
            isOtpSent = true
            otpTimer = 60
        }
    }

    LaunchedEffect(otpTimer) {
        if (otpTimer > 0) {
            kotlinx.coroutines.delay(1000)
            otpTimer--
        }
    }

    val logoLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { logoPath = copyUriToInternalStorage(context, it, "shop_logo.png") }
            }

    ConfigCard {
        Text(
                "Shop Logo Upload",
                color = PrimaryGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                    modifier =
                            Modifier.size(100.dp)
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray),
                    contentAlignment = Alignment.Center
            ) {
                val currentLogo = logoPath
                if (currentLogo != null) {
                    loadBitmap(currentLogo)?.let {
                        Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                } else {
                    Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                    )
                }
            }
            OutlinedButton(
                    onClick = { logoLauncher.launch("image/*") },
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGold),
                    shape = RoundedCornerShape(20.dp)
            ) { Text("Change Logo", color = PrimaryGold, fontSize = 14.sp) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ParchmentTextField(
                value = name,
                onValueChange = { name = it },
                label = "Shop Name",
                isError = !isNameValid,
                supportingText = if (!isNameValid) "Too short" else null
        )
        Spacer(modifier = Modifier.height(12.dp))
        ParchmentTextField(
                value = address,
                onValueChange = { address = it },
                label = "Shop Address",
                isError = !isAddressValid,
                supportingText = if (!isAddressValid) "Cannot be empty" else null
        )
        Spacer(modifier = Modifier.height(12.dp))

        ParchmentTextField(
                value = whatsapp,
                onValueChange = {
                    if (it.length <= 10) {
                        whatsapp = it
                        isVerified = (it == profile?.whatsappNumber)
                        isOtpSent = false
                    }
                },
                label = "Whatsapp Number",
                isError = whatsapp.isNotEmpty() && !isValidPhone(whatsapp),
                supportingText =
                        if (whatsapp.isNotEmpty() && !isValidPhone(whatsapp))
                                "Invalid 10-digit number"
                        else null,
                trailingIcon = {
                    if (isVerified) {
                        Icon(Icons.Default.CheckCircle, null, tint = VegGreen)
                    } else if (isValidPhone(whatsapp)) {
                        TextButton(onClick = { authViewModel.sendOtp(whatsapp) }) {
                            Text("Verify", color = PrimaryGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
        )

        if (isOtpSent && !isVerified) {
            Spacer(modifier = Modifier.height(12.dp))
            ParchmentTextField(
                    value = otpValue,
                    onValueChange = {
                        if (it.length <= 6) {
                            otpValue = it
                            if (it.length == 6) {
                                if (authViewModel.verifyOtp(it)) {
                                    isVerified = true
                                    isOtpSent = false
                                    Toast.makeText(
                                                    context,
                                                    "âœ“ WhatsApp number verified!",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                }
                            }
                        }
                    },
                    label = "Enter 6-digit OTP",
                    isError = otpValue.length == 6 && !isVerified,
                    supportingText =
                            if (otpValue.length == 6 && !isVerified) "Invalid OTP" else null,
                    trailingIcon = {
                        if (otpTimer > 0) {
                            Text(
                                    "${otpTimer}s",
                                    color = TextGold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                            )
                        } else {
                            TextButton(onClick = { authViewModel.sendOtp(whatsapp) }) {
                                Text("Resend", color = PrimaryGold, fontSize = 12.sp)
                            }
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        ParchmentTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                isError = !isEmailValid,
                supportingText = if (!isEmailValid) "Invalid email format" else null
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                    checked = consent,
                    onCheckedChange = { consent = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGold)
            )
            Text("I consent to receive invoice copies on Email", color = TextGold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isSaveEnabled = isNameValid && isAddressValid && isEmailValid && isVerified
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                    onClick = {
                        if (isSaveEnabled) {
                            profile?.copy(
                                            shopName = name,
                                            shopAddress = address,
                                            whatsappNumber = whatsapp,
                                            email = email,
                                            logoPath = logoPath,
                                            emailInvoiceConsent = consent
                                    )
                                    ?.let { viewModel.saveProfile(it) }
                            Toast.makeText(context, "Shop profile saved", Toast.LENGTH_SHORT).show()
                        }
                        onBack()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = if (isSaveEnabled) SuccessGreen else Color.Gray
                            ),
                    shape = RoundedCornerShape(24.dp),
                    enabled = isSaveEnabled
            ) { Text("Save", color = Color.White) }

            OutlinedButton(
                    onClick = {
                        viewModel.resetDailyCounter()
                        Toast.makeText(context, "Daily order counter reset", Toast.LENGTH_SHORT)
                                .show()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    shape = RoundedCornerShape(24.dp)
            ) { Text("Reset Counter", fontSize = 11.sp) }
        }
    }
}

@Composable
private fun SettingsItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        text: String,
        onClick: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = CardBG),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold.copy(alpha = 0.3f))
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        icon,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text, color = TextLight, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PrimaryGold
            )
        }
    }
}

@Composable
private fun PaymentConfigView(
        profile: RestaurantProfileEntity?,
        onSave: (RestaurantProfileEntity) -> Unit,
        onBack: () -> Unit
) {
    val context = LocalContext.current
    var currency by remember { mutableStateOf(profile?.currency ?: "INR") }
    var country by remember { mutableStateOf(profile?.country ?: "India") }
    var upiSupported by remember { mutableStateOf(profile?.upiEnabled ?: false) }
    var upiHandle by remember { mutableStateOf(profile?.upiHandle ?: "") }
    var upiMobile by remember { mutableStateOf(profile?.upiMobile ?: "") }
    var qrPath by remember { mutableStateOf(profile?.upiQrPath) }

    // New payment mode toggles
    var cashEnabled by remember { mutableStateOf(profile?.cashEnabled ?: true) }
    var posEnabled by remember { mutableStateOf(profile?.posEnabled ?: false) }
    var zomatoEnabled by remember { mutableStateOf(profile?.zomatoEnabled ?: false) }
    var swiggyEnabled by remember { mutableStateOf(profile?.swiggyEnabled ?: false) }
    var ownWebsiteEnabled by remember { mutableStateOf(profile?.ownWebsiteEnabled ?: false) }

    val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { qrPath = copyUriToInternalStorage(context, it, "upi_qr.png") }
            }

    ConfigCard {
        ParchmentTextField(
                value = currency,
                onValueChange = { currency = it },
                label = "Currency *"
        )
        Spacer(modifier = Modifier.height(12.dp))
        ParchmentTextField(value = country, onValueChange = { country = it }, label = "Country *")

        Spacer(modifier = Modifier.height(24.dp))
        Text(
                "Enable Payment Methods",
                color = PrimaryGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        PaymentToggle("Cash Payment", cashEnabled) { cashEnabled = it }
        PaymentToggle("POS Machine", posEnabled) { posEnabled = it }
        PaymentToggle("Zomato Orders", zomatoEnabled) { zomatoEnabled = it }
        PaymentToggle("Swiggy Orders", swiggyEnabled) { swiggyEnabled = it }
        PaymentToggle("Own Website", ownWebsiteEnabled) { ownWebsiteEnabled = it }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                    checked = upiSupported,
                    onCheckedChange = { upiSupported = it },
                    colors = CheckboxDefaults.colors(checkedColor = SuccessGreen)
            )
            Text("UPI supported in your country", color = TextGold, fontSize = 14.sp)
        }

        if (upiSupported) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                        modifier =
                                Modifier.size(100.dp)
                                        .background(Color.White)
                                        .border(1.dp, Color.LightGray)
                                        .padding(4.dp),
                        contentAlignment = Alignment.Center
                ) {
                    val currentQr = qrPath
                    if (currentQr != null) {
                        loadBitmap(currentQr)?.let {
                            Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                        )
                    }
                }
                OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGold),
                        shape = RoundedCornerShape(20.dp)
                ) { Text("Upload QR Code >", color = PrimaryGold, fontSize = 14.sp) }
            }
            Spacer(modifier = Modifier.height(20.dp))
            ParchmentTextField(
                    value = upiHandle,
                    onValueChange = { upiHandle = it },
                    label = "UPI Handle"
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParchmentTextField(
                    value = upiMobile,
                    onValueChange = { upiMobile = it },
                    label = "UPI Mobile Number"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                    onClick = {
                        profile?.copy(
                                        currency = currency,
                                        country = country,
                                        upiEnabled = upiSupported,
                                        upiHandle = upiHandle,
                                        upiMobile = upiMobile,
                                        upiQrPath = qrPath,
                                        cashEnabled = cashEnabled,
                                        posEnabled = posEnabled,
                                        zomatoEnabled = zomatoEnabled,
                                        swiggyEnabled = swiggyEnabled,
                                        ownWebsiteEnabled = ownWebsiteEnabled
                                )
                                ?.let { onSave(it) }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(24.dp)
            ) { Text("Save", color = Color.White) }

            OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextGold),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold)
            ) { Text("Back") }
        }
    }
}

@Composable
fun PaymentToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGold, fontSize = 14.sp)
        Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = SuccessGreen)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrinterConfigView(
        profile: RestaurantProfileEntity?,
        onSave: (RestaurantProfileEntity) -> Unit,
        onBack: () -> Unit,
        viewModel: SettingsViewModel
) {
    var enabled by remember { mutableStateOf(profile?.printerEnabled ?: false) }
    var paper58 by remember { mutableStateOf((profile?.paperSize ?: "58mm") == "58mm") }
    var autoPrint by remember { mutableStateOf(profile?.autoPrintOnSuccess ?: false) }
    var includeLogo by remember { mutableStateOf(profile?.includeLogoInPrint ?: true) }
    var printWhatsapp by remember { mutableStateOf(profile?.printCustomerWhatsapp ?: true) }

    val context = LocalContext.current
    
    // Reactive Bluetooth state tracking
    var isBtActive by remember { mutableStateOf(viewModel.isBluetoothEnabled(context)) }
    
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    isBtActive = (state == BluetoothAdapter.STATE_ON)
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    // BT ViewModel state
    val btDevices by viewModel.btDevices.collectAsState()
    val btIsScanning by viewModel.btIsScanning.collectAsState()
    val btIsConnecting by viewModel.btIsConnecting.collectAsState()
    val btConnectResult by viewModel.btConnectResult.collectAsState()

    var showBtSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permission launcher (Android 12+)
    val permissionLauncher =
            rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
            ) { grants ->
                val allGranted = grants.values.all { it }
                if (allGranted) {
                    showBtSheet = true
                    viewModel.startBluetoothScan(context)
                } else {
                    Toast.makeText(context, "Bluetooth permission denied", Toast.LENGTH_SHORT)
                            .show()
                }
            }

    // BT enable launcher
    val enableBtLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (viewModel.isBluetoothEnabled(context)) {
                    showBtSheet = true
                    viewModel.startBluetoothScan(context)
                }
            }

    // React to connect result
    LaunchedEffect(btConnectResult) {
        btConnectResult?.let { ok ->
            Toast.makeText(
                            context,
                            if (ok) "âœ“ Printer connected successfully!"
                            else "âœ— Connection failed. Try again.",
                            Toast.LENGTH_SHORT
                    )
                    .show()
            if (ok) showBtSheet = false
            viewModel.clearBtConnectResult()
        }
    }

    fun ensureBluetoothAndPermissions(onReady: () -> Unit = {}) {
        viewModel.initBluetooth(context)
        if (!viewModel.hasBluetoothPermissions(context)) {
            val perms =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                        )
                    } else {
                        arrayOf(Manifest.permission.BLUETOOTH)
                    }
            permissionLauncher.launch(perms)
        } else if (!viewModel.isBluetoothEnabled(context)) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            onReady()
        }
    }

    fun launchScan() {
        ensureBluetoothAndPermissions {
            showBtSheet = true
            viewModel.startBluetoothScan(context)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bluetooth Connection Section
        PrinterSectionHeader(icon = Icons.Default.Bluetooth, title = "Bluetooth Printer Connection")
        ConfigCard {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        "Enable Bluetooth Printer",
                        color = TextGold,
                        fontWeight = FontWeight.Medium
                )
                Switch(
                        checked = enabled,
                        onCheckedChange = { 
                            enabled = it
                            if (it) {
                                ensureBluetoothAndPermissions()
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
                )
            }

            // Warning if enabled but Bluetooth is OFF
            if (enabled && !isBtActive) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Bluetooth is OFF. Please turn it ON to use the printer.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Connected printer info card
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .border(
                                                1.dp,
                                                BorderGold.copy(alpha = 0.5f),
                                                RoundedCornerShape(4.dp)
                                        )
                                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    Icons.Default.Print,
                                    null,
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    "Connected Printer",
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text("Printer Name  ", color = TextGold, fontSize = 13.sp)
                            Text(
                                    profile?.printerName ?: "Not Connected",
                                    color = TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                            )
                        }
                        Row {
                            Text("MAC Address  ", color = TextGold, fontSize = 13.sp)
                            Text(
                                    profile?.printerMac ?: "None",
                                    color = TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Status: ", color = TextGold, fontSize = 13.sp)
                            val connected = profile?.printerMac != null
                            Box(
                                    modifier =
                                            Modifier.size(8.dp)
                                                    .background(
                                                            if (connected) Color(0xFF4CAF50)
                                                            else Color.Red,
                                                            RoundedCornerShape(4.dp)
                                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                    if (connected) "Connected" else "Not Connected",
                                    color = if (connected) Color(0xFF2E7D32) else Color.Red,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scan button
                Button(
                        onClick = { launchScan() },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                        shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                            Icons.Default.Search,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan for Printers", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Paper Size Section
        PrinterSectionHeader(icon = Icons.Default.Description, title = "Receipt Paper Size")
        ConfigCard {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                            selected = paper58,
                            onClick = { paper58 = true },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold)
                    )
                    Text(
                            "58mm (Default small thermal printer)",
                            color = TextGold,
                            fontSize = 14.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                        selected = !paper58,
                        onClick = { paper58 = false },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold)
                )
                Text("80mm (Large billing printer)", color = TextGold, fontSize = 14.sp)
            }
        }

        // Print Options Section
        PrinterSectionHeader(icon = Icons.Default.Print, title = "Print Options")
        ConfigCard {
            PrinterOptionRow("Auto Print after Payment Success", autoPrint) { autoPrint = it }
            PrinterOptionRow("Include Logo in Print", includeLogo) { includeLogo = it }
            PrinterOptionRow("Print Customer WhatsApp Number", printWhatsapp) { printWhatsapp = it }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Test Print button
            Button(
                    onClick = {
                        val btMgr = BluetoothPrinterManager(context)
                        if (!btMgr.isConnected()) {
                            Toast.makeText(
                                            context,
                                            "No printer connected. Scan and connect first.",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        } else {
                            // ESC/POS test page
                            val esc = 0x1B.toByte()
                            val testData =
                                    buildList {
                                                add(esc)
                                                add('@'.code.toByte()) // Initialize
                                                add(esc)
                                                add('!'.code.toByte())
                                                add(0x08) // Double height
                                                addAll(
                                                        "=== TEST PRINT ===\n"
                                                                .toByteArray()
                                                                .toList()
                                                )
                                                add(esc)
                                                add('!'.code.toByte())
                                                add(0x00) // Normal
                                                addAll("KhanaBook Lite\n".toByteArray().toList())
                                                addAll("Printer OK!\n\n\n".toByteArray().toList())
                                                add(0x1D)
                                                add('V'.code.toByte())
                                                add(0x41)
                                                add(0x10) // Cut
                                            }
                                            .toByteArray()
                            val ok = btMgr.printBytes(testData)
                            Toast.makeText(
                                            context,
                                            if (ok) "Test page sent!" else "Print failed",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ParchmentBG),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037)),
                    shape = RoundedCornerShape(4.dp)
            ) { Text("TEST PRINT", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold) }

            Button(
                    onClick = {
                        profile?.copy(
                                        printerEnabled = enabled,
                                        paperSize = if (paper58) "58mm" else "80mm",
                                        autoPrintOnSuccess = autoPrint,
                                        includeLogoInPrint = includeLogo,
                                        printCustomerWhatsapp = printWhatsapp
                                )
                                ?.let { onSave(it) }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(4.dp)
            ) { Text("SAVE SETTINGS", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    // â”€â”€ Bluetooth Device Picker Bottom-Sheet â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    if (showBtSheet) {
        ModalBottomSheet(
                onDismissRequest = {
                    viewModel.stopBluetoothScan()
                    showBtSheet = false
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1C1008)
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 32.dp)
            ) {
                // Sheet header
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            "Select Printer",
                            color = PrimaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                    )
                    if (btIsScanning) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PrimaryGold,
                                    strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scanning...", color = TextGold, fontSize = 12.sp)
                        }
                    } else {
                        TextButton(onClick = { viewModel.startBluetoothScan(context) }) {
                            Icon(
                                    Icons.Default.Refresh,
                                    null,
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rescan", color = PrimaryGold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        "Paired devices appear first. Make sure your printer is powered ON.",
                        color = TextGold.copy(alpha = 0.7f),
                        fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (btDevices.isEmpty() && !btIsScanning) {
                    Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                    Icons.Default.BluetoothSearching,
                                    null,
                                    tint = PrimaryGold.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    "No devices found",
                                    color = TextGold.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 340.dp)
                    ) {
                        items(btDevices, key = { it.address }) { device ->
                            val deviceName =
                                    try {
                                        device.name ?: "Unknown Device"
                                    } catch (_: Exception) {
                                        "Unknown Device"
                                    }
                            val mac = device.address
                            val isPaired =
                                    try {
                                        device.bondState ==
                                                android.bluetooth.BluetoothDevice.BOND_BONDED
                                    } catch (_: Exception) {
                                        false
                                    }
                            val isCurrentPrinter = profile?.printerMac == mac

                            Card(
                                    modifier =
                                            Modifier.fillMaxWidth().clickable(
                                                            enabled = !btIsConnecting
                                                    ) {
                                                viewModel.connectToPrinter(context, device)
                                            },
                                    colors =
                                            CardDefaults.cardColors(
                                                    containerColor =
                                                            if (isCurrentPrinter)
                                                                    Color(0xFF2E7D32)
                                                                            .copy(alpha = 0.2f)
                                                            else Color(0xFF2D1B10)
                                            ),
                                    border =
                                            androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isCurrentPrinter)
                                                            Color(0xFF4CAF50).copy(alpha = 0.6f)
                                                    else BorderGold.copy(alpha = 0.2f)
                                            ),
                                    shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                Icons.Default.BluetoothConnected,
                                                null,
                                                tint =
                                                        if (isCurrentPrinter) Color(0xFF4CAF50)
                                                        else PrimaryGold,
                                                modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                    deviceName,
                                                    color = TextLight,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                    mac,
                                                    color = TextGold.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (isPaired) {
                                            Surface(
                                                    color = Color(0xFF5D4037).copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                        "PAIRED",
                                                        color = PrimaryGold,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier =
                                                                Modifier.padding(
                                                                        horizontal = 6.dp,
                                                                        vertical = 2.dp
                                                                )
                                                )
                                            }
                                        }
                                        if (btIsConnecting && isCurrentPrinter) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = PrimaryGold,
                                                    strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Connecting overlay
                if (btIsConnecting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryGold,
                                strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Connecting...", color = TextGold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrinterSectionHeader(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String
) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFF2D1B16), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF5D4037), RoundedCornerShape(4.dp))
                            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                    icon,
                    null,
                    tint = Color.White,
                    modifier =
                            Modifier.size(20.dp)
                                    .background(Color(0xFF4A90E2), RoundedCornerShape(4.dp))
                                    .padding(2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PrinterOptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGold)
        )
        Text(label, color = TextGold, fontSize = 14.sp)
    }
}

@Composable
private fun TaxConfigView(
        profile: RestaurantProfileEntity?,
        onSave: (RestaurantProfileEntity) -> Unit,
        onBack: () -> Unit
) {
    var country by remember { mutableStateOf(profile?.country ?: "India") }
    var gstEnabled by remember { mutableStateOf(profile?.gstEnabled ?: false) }
    var gstNumber by remember { mutableStateOf(profile?.gstin ?: "") }
    var gstPct by remember { mutableStateOf((profile?.gstPercentage ?: 0.0).toString()) }
    var fssaiNumber by remember { mutableStateOf(profile?.fssaiNumber ?: "") }

    val isGstValid = !gstEnabled || !country.equals("India", true) || isValidGst(gstNumber)
    val isPctValid = isValidTaxPercentage(gstPct)
    val isFssaiValid =
            fssaiNumber.isNotBlank() && fssaiNumber.length >= 10 // Basic length check for FSSAI
    val isSaveEnabled = isGstValid && isPctValid && isFssaiValid

    ConfigCard {
        ParchmentTextField(
                value = country,
                onValueChange = { country = it },
                label = "Select Country"
        )

        Spacer(modifier = Modifier.height(16.dp))
        ParchmentTextField(
                value = fssaiNumber,
                onValueChange = { fssaiNumber = it },
                label = "FSSAI Number *",
                isError = !isFssaiValid && fssaiNumber.isNotEmpty(),
                supportingText =
                        if (!isFssaiValid && fssaiNumber.isNotEmpty()) "Enter a valid FSSAI number"
                        else "Mandatory field"
        )

        if (country.equals("India", ignoreCase = true)) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text("GST Number Available", color = TextGold, fontWeight = FontWeight.Medium)
                Switch(
                        checked = gstEnabled,
                        onCheckedChange = { gstEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
                )
            }
            if (gstEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                ParchmentTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it.uppercase() },
                        label = "GST Number",
                        isError = !isGstValid,
                        supportingText = if (!isGstValid) "Invalid format" else null
                )
                Spacer(modifier = Modifier.height(12.dp))
                ParchmentTextField(
                        value = gstPct,
                        onValueChange = { gstPct = it },
                        label = "GST Percentage (%)",
                        isError = !isPctValid,
                        supportingText = if (!isPctValid) "Must be between 0 and 100" else null
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                    onClick = {
                        if (isSaveEnabled) {
                            val pct = gstPct.toDoubleOrNull() ?: 0.0
                            profile?.copy(
                                            country = country,
                                            gstEnabled = gstEnabled,
                                            gstin = gstNumber,
                                            gstPercentage = pct,
                                            fssaiNumber = fssaiNumber
                                    )
                                    ?.let { onSave(it) }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = if (isSaveEnabled) SuccessGreen else Color.Gray
                            ),
                    enabled = isSaveEnabled
            ) { Text("Save", fontWeight = FontWeight.Bold) }

            OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextGold),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold)
            ) { Text("Back") }
        }
    }
}

@Composable
fun ParchmentTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        trailingIcon: @Composable (() -> Unit)? = null,
        isError: Boolean = false,
        supportingText: String? = null
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 12.sp, color = TextGold) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            trailingIcon = trailingIcon,
            isError = isError,
            supportingText =
                    supportingText?.let { { Text(it, color = DangerRed, fontSize = 11.sp) } },
            colors =
                    OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderGold,
                            unfocusedBorderColor = BorderGold.copy(alpha = 0.5f),
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedLabelColor = TextGold,
                            unfocusedLabelColor = TextGold.copy(alpha = 0.7f),
                            cursorColor = PrimaryGold,
                            errorBorderColor = DangerRed,
                            errorLabelColor = DangerRed,
                            errorCursorColor = DangerRed
                    ),
            singleLine = true
    )
}

private fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            File(context.filesDir, fileName).let { file ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
                file.absolutePath
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun loadBitmap(path: String): Bitmap? {
    return try {
        BitmapFactory.decodeFile(path)
    } catch (_: Exception) {
        null
    }
}

