package com.khanabook.lite.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class TabItem(val label: String, val icon: ImageVector, val originalIndex: Int)

object NavigationUtils {
    private val allTabs = listOf(
        TabItem("Home", Icons.Default.Home, 0),
        TabItem("Inventory", Icons.Default.Inventory, 1),
        TabItem("Reports", Icons.Default.Assessment, 2),
        TabItem("Orders", Icons.AutoMirrored.Filled.List, 3),
        TabItem("Settings", Icons.Default.Settings, 4)
    )

    fun getVisibleTabs(role: String?): List<TabItem> {
        return if (role == "staff") {
            allTabs.filter { it.label == "Home" || it.label == "Orders" || it.label == "Inventory" }
        } else {
            allTabs
        }
    }
}


