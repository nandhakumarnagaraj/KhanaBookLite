package com.khanabook.lite.pos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.khanabook.lite.pos.ui.screens.InventoryScreen
import com.khanabook.lite.pos.ui.screens.HomeScreen
import com.khanabook.lite.pos.ui.screens.ReportsScreen
import com.khanabook.lite.pos.ui.screens.OrdersScreen
import com.khanabook.lite.pos.ui.screens.SettingsScreen
import com.khanabook.lite.pos.ui.screens.StaffManagementScreen
import com.khanabook.lite.pos.ui.theme.DarkBrown1
import com.khanabook.lite.pos.ui.theme.PrimaryGold
import com.khanabook.lite.pos.ui.theme.TextLight

import com.khanabook.lite.pos.data.local.entity.UserEntity
import com.khanabook.lite.pos.ui.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.khanabook.lite.pos.ui.navigation.TabItem
import com.khanabook.lite.pos.ui.navigation.NavigationUtils

@Composable
fun MainScreen(
    initialTab: Int = 0,
    onNewBill: () -> Unit,
    onSearchBill: () -> Unit,
    onOrderStatus: () -> Unit,
    onCallCustomer: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val role = currentUser?.role ?: "staff"
    
    val visibleTabs = remember(role) {
        NavigationUtils.getVisibleTabs(role)
    }

    var selectedTabIndex by remember(initialTab, visibleTabs) { 
        val initialVisibleIndex = visibleTabs.indexOfFirst { it.originalIndex == initialTab }
        mutableIntStateOf(if (initialVisibleIndex != -1) initialVisibleIndex else 0) 
    }
    
    Scaffold(
        bottomBar = {
            AppBottomBar(
                visibleTabs = visibleTabs,
                currentSelectedIndex = selectedTabIndex, 
                onTabSelected = { selectedTabIndex = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val currentTab = visibleTabs[selectedTabIndex]
            val backToHome = { selectedTabIndex = 0 }
            when (currentTab.label) {
                "Home" -> HomeScreen(onNewBill, onSearchBill, onOrderStatus, onCallCustomer)
                "Inventory" -> InventoryScreen(onBack = backToHome)
                "Reports" -> ReportsScreen(onBack = backToHome)
                "Orders" -> OrdersScreen(onBack = backToHome)
                "Settings" -> SettingsScreen(onBack = backToHome)
            }
        }
    }
}

@Composable
fun AppBottomBar(
    visibleTabs: List<TabItem>,
    currentSelectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(containerColor = DarkBrown1) {
        visibleTabs.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = currentSelectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGold,
                    unselectedIconColor = TextLight,
                    selectedTextColor = PrimaryGold,
                    unselectedTextColor = TextLight,
                    indicatorColor = DarkBrown1
                )
            )
        }
    }
}

