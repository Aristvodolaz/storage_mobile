package com.komus.sorage_mobile.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.Icon
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.komus.sorage_mobile.presentation.screens.razmechenie.ProductInfoScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.ScanLocationScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.ScanBufferLocationScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.SearchScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.UnitSelectionScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.ExpirationDateScreen
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.util.Screen
import com.komus.sorage_mobile.presentation.screens.search.ProductSearchScreen
import com.komus.sorage_mobile.presentation.screens.snyatie.ScanLocationScreen as SnyatieScanLocationScreen
import com.komus.sorage_mobile.presentation.screens.movement.ScanSourceLocationScreen
import com.komus.sorage_mobile.presentation.screens.info.ProductInfoScreen
import com.komus.sorage_mobile.presentation.screens.inventory.InventoryScreen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.os.Build
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.presentation.components.TopConnectionStatusBar

object Routes {
    const val AUTH = "auth"
    const val MAIN = "inventory"
    const val PLACEMENT = "placement"
    const val PLACEMENT_SCAN_LOCATION = "placement_scan_location"
    const val PLACEMENT_SCAN_PRODUCT = "placement_scan_product"
    const val PLACEMENT_EXPIRATION_DATE = "placement_expiration_date"
    const val SNYATIE_SCAN_LOCATION = "snyatie_scan_location"
    const val MOVE_PRODUCT = "move_product"
}

@Composable
fun MainScreen(spHelper: SPHelper,
               scannerViewModel: ScannerViewModel ) {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = { 
                // Добавляем отступ для системной статус-бары и показываем TopBar
                Box(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    TopConnectionStatusBar()
                }
            },
            bottomBar = { 
                // Добавляем отступ для системной навигации и показываем BottomBar
                Box(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    BottomNavigationBar(navController)
                }
            },
            // Отключаем встроенные отступы Scaffold
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            NavigationGraph(navController, paddingValues, scannerViewModel, spHelper)
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Inventory, Screen.Placement, Screen.Removal, Screen.Movement, Screen.Info
    )
    BottomNavigation(
        backgroundColor = Color(0xFF6200EE), 
        contentColor = Color.White,
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val currentRoute = currentRoute(navController)
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.LightGray
            )
        }
    }
}
@Composable
fun NavigationGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    scannerViewModel: ScannerViewModel,
    spHelper: SPHelper
) {
    // Учитываем paddingValues для отступов контента от верхней и нижней части экрана
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            )
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Inventory.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Inventory.route) {
             InventoryScreen(scannerViewModel = scannerViewModel)
            }

            composable(Screen.Placement.route){
                SearchScreen(
                    navController = navController,
                    scannerViewModel = scannerViewModel,
                    spHelper = spHelper
                ) {}
            }
            
            // Экран снятия товара
            composable(Screen.Removal.route) {
                SnyatieScanLocationScreen(
                    navController = navController, 
                    scannerViewModel = scannerViewModel,
                    spHelper = spHelper
                )
            }
            
            // Экран перемещения товара
            composable(Screen.Movement.route) {
                ScanSourceLocationScreen(navController, scannerViewModel)
            }
            
            composable(Screen.Info.route) { 
                ProductInfoScreen(navController = navController, scannerViewModel = scannerViewModel)
            }

            composable("search_results/{query}") { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query") ?: ""
                UnitSelectionScreen(navController, productId =  query, spHelper = spHelper)
            }

            composable("product_info") {
                ProductInfoScreen(navController, spHelper = spHelper)
            }
            
            composable("scan_ir_location") { 
                ScanLocationScreen(navController, spHelper = spHelper, scannerViewModel)
            }
            
            composable("scan_buffer_location") {
                ScanBufferLocationScreen(navController, spHelper = spHelper, scannerViewModel)
            }
            
            // Экран срока годности
            composable("expiration_date") { 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ExpirationDateScreen(navController)
                } else {
                    // Для устройств с API ниже 26 (Android 8.0)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Эта функция требует Android 8.0 или выше")
                    }
                }
            }

            // Экран поиска товаров
            composable("product_search") {
                ProductSearchScreen(navController = navController, scannerViewModel = scannerViewModel)
            }

            composable(Routes.MOVE_PRODUCT) {
                ScanSourceLocationScreen(
                    navController = navController,
                    scannerViewModel = scannerViewModel
                )
            }


        }
    }
}

@Composable
fun ScreenContent(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
