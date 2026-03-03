package com.khanabooklite.app.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.entity.*
import com.khanabooklite.app.data.repository.CategoryRepository
import com.khanabooklite.app.data.repository.MenuRepository
import com.khanabooklite.app.data.repository.RestaurantRepository
import com.khanabooklite.app.data.repository.UserRepository
import com.khanabooklite.app.domain.manager.BluetoothPrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val menuRepository: MenuRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val profile: StateFlow<RestaurantProfileEntity?> = restaurantRepository.getProfileFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // ── Bluetooth Printer ─────────────────────────────────────────────────────

    private var btManager: BluetoothPrinterManager? = null

    /** Lazily-initialised BT state, forwarded from [BluetoothPrinterManager]. */
    private val _btDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val btDevices: StateFlow<List<BluetoothDevice>> = _btDevices.asStateFlow()

    private val _btIsScanning = MutableStateFlow(false)
    val btIsScanning: StateFlow<Boolean> = _btIsScanning.asStateFlow()

    private val _btIsConnecting = MutableStateFlow(false)
    val btIsConnecting: StateFlow<Boolean> = _btIsConnecting.asStateFlow()

    /** Result of the last connect attempt: null = idle, true = success, false = failed */
    private val _btConnectResult = MutableStateFlow<Boolean?>(null)
    val btConnectResult: StateFlow<Boolean?> = _btConnectResult.asStateFlow()

    fun initBluetooth(context: Context) {
        if (btManager == null) {
            btManager = BluetoothPrinterManager(context)
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        initBluetooth(context)
        return btManager?.isBluetoothEnabled() == true
    }

    fun hasBluetoothPermissions(context: Context): Boolean {
        initBluetooth(context)
        return btManager?.hasRequiredPermissions() == true
    }

    /** Start BT discovery — populates [btDevices] reactively. */
    fun startBluetoothScan(context: Context) {
        initBluetooth(context)
        val mgr = btManager ?: return
        viewModelScope.launch {
            // Forward state from manager into our own StateFlow so Compose can observe
            mgr.scannedDevices.collect { _btDevices.value = it }
        }
        viewModelScope.launch {
            mgr.isScanning.collect { _btIsScanning.value = it }
        }
        mgr.startScan()
    }

    fun stopBluetoothScan() {
        btManager?.stopScan()
        _btIsScanning.value = false
    }

    /**
     * Connects to [device] on an IO thread, then persists the printer
     * name+mac into [RestaurantProfileEntity] on success.
     */
    @Suppress("MissingPermission")
    fun connectToPrinter(context: Context, device: BluetoothDevice) {
        initBluetooth(context)
        val mgr = btManager ?: return
        _btConnectResult.value = null
        _btIsConnecting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val ok = mgr.connect(device)
            _btIsConnecting.value = false
            _btConnectResult.value = ok
            if (ok) {
                val name = try { device.name ?: "BT Printer" } catch (_: Exception) { "BT Printer" }
                val mac  = device.address
                // Persist to profile so it survives restarts
                val current = restaurantRepository.getProfile()
                current?.copy(printerName = name, printerMac = mac)?.let {
                    restaurantRepository.saveProfile(it)
                }
            }
        }
    }

    fun clearBtConnectResult() {
        _btConnectResult.value = null
    }

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveProfile(profile: RestaurantProfileEntity) {
        viewModelScope.launch {
            restaurantRepository.saveProfile(profile)
            // Synchronize phone number with admin user login ID
            userRepository.updateAdminPhoneNumber(profile.whatsappNumber)
            
            // Refresh current user if they are the admin to reflect the new ID
            userRepository.currentUser.value?.let { current ->
                if (current.role == "admin") {
                    userRepository.setCurrentUser(current.copy(
                        email = profile.whatsappNumber,
                        whatsappNumber = profile.whatsappNumber
                    ))
                }
            }
        }
    }

    // Category CRUD
    fun addCategory(name: String) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            categoryRepository.insertCategory(CategoryEntity(name = name, isVeg = true, createdAt = now))
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    // Menu Item CRUD
    fun addItem(categoryId: Int, name: String, price: Double, type: String, stock: Int) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            menuRepository.insertItem(MenuItemEntity(
                categoryId = categoryId,
                name = name,
                basePrice = price,
                foodType = type,
                stockQuantity = stock,
                createdAt = now
            ))
        }
    }

    fun updateItem(item: MenuItemEntity) {
        viewModelScope.launch {
            menuRepository.updateItem(item)
        }
    }

    fun toggleItemAvailability(id: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            menuRepository.toggleItemAvailability(id, isAvailable)
        }
    }

    fun deleteItem(item: MenuItemEntity) {
        viewModelScope.launch {
            menuRepository.deleteItem(item)
        }
    }

    fun resetDailyCounter() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            restaurantRepository.resetDailyCounter(0, today)
        }
    }

    fun getItemsByCategory(categoryId: Int) = menuRepository.getItemsByCategoryFlow(categoryId)

    fun backupDatabase(context: android.content.Context, onResult: (Boolean) -> Unit) {
        val backupManager = com.khanabooklite.app.domain.manager.BackupManager(context)
        val exportDir = context.getExternalFilesDir(null) ?: context.filesDir
        val success = backupManager.exportDatabase("khana_book_lite.db", exportDir)
        onResult(success)
    }

    fun restoreDatabase(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val backupManager = com.khanabooklite.app.domain.manager.BackupManager(context)
                val tempFile = File(context.cacheDir, "temp_restore.db")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val success = backupManager.importDatabase("khana_book_lite.db", tempFile)
                tempFile.delete()
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
