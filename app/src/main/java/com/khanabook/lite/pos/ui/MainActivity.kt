package com.khanabook.lite.pos.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.khanabook.lite.pos.ui.navigation.*
import com.khanabook.lite.pos.ui.screens.*
import com.khanabook.lite.pos.ui.theme.KhanaBookLiteTheme
import com.khanabook.lite.pos.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KhanaBookLiteTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val currentUser by authViewModel.currentUser.collectAsState()



                // Global Logout Listener
                LaunchedEffect(currentUser) {
                    val currentRoute = navController.currentDestination?.route
                    if (currentUser == null && currentRoute != "splash") {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                }

                val navigateToMainTab: (Int) -> Unit = { tab ->
                    navController.navigate("main/$tab") {
                        launchSingleTop = true
                        popUpTo("main/{tab}") { saveState = true }
                        restoreState = true
                    }
                }

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(
                                onTimeout = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("main/0") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onSignUpClick = { navController.navigate("signup") },

                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                                onSignUpSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onLoginClick = { navController.popBackStack() }
                        )
                    }
                    composable("main/{tab}") { backStackEntry ->
                        val selectedTab =
                                backStackEntry.arguments?.getString("tab")?.toIntOrNull() ?: 0
                        MainScreen(
                                initialTab = selectedTab,
                                onNewBill = { navController.navigate("new_bill") },
                                onSearchBill = { navController.navigate("search_bill") },
                                onOrderStatus = { navController.navigate("order_status") },
                                onCallCustomer = { navController.navigate("call_customer") }
                        )
                    }
                    composable("new_bill") {
                        NewBillScreen(
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable("search_bill") {
                        SearchScreen(
                                title = "Search & Bill",
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable("order_status") {
                        SearchScreen(
                                title = "Check Order Status",
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable("call_customer") {
                        CallCustomerScreen(
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
