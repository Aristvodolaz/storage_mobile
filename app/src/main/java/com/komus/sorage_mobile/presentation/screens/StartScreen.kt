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
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.presentation.screens.razmechenie.ProductInfoScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.ScanLocationScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.ScanBufferLocationScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.SearchScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.UnitSelectionScreen
import com.komus.sorage_mobile.presentation.screens.razmechenie.ExpirationDateScreen
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.util.Screen
import com.komus.sorage_mobile.presentation.screens.peremischenie.ConfirmationScreen
import com.komus.sorage_mobile.presentation.screens.peremischenie.ProductIdScreen
import com.komus.sorage_mobile.presentation.screens.peremischenie.QuantityInputScreen
import com.komus.sorage_mobile.presentation.screens.peremischenie.SourceLocationScreen
import com.komus.sorage_mobile.presentation.screens.peremischenie.TargetLocationScreen
import com.komus.sorage_mobile.presentation.screens.search.ProductSearchScreen
import com.komus.sorage_mobile.presentation.screens.snyatie.ScanLocationScreen as SnyatieScanLocationScreen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.os.Build
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.sorage_mobile.domain.viewModel.PickViewModel
import com.komus.sorage_mobile.domain.viewModel.PlacementViewModel
import com.komus.sorage_mobile.presentation.components.TopConnectionStatusBar

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
                // Добавляем отступ для системной статус-бары
                Box(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    TopConnectionStatusBar()
                }
            },
            bottomBar = { 
                // Добавляем отступ для системной навигации
                Box(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    BottomNavigationBar(navController)
                }
            },
            // Отключаем встроенные отступы Scaffold, чтобы управлять ими вручную
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            // Используем padding из paddingValues для корректного отображения контента
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavigationGraph(navController, spHelper, scannerViewModel)
            }
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
fun NavigationGraph(navController: NavHostController,
                    spHelper: SPHelper,
                    scannerViewModel: ScannerViewModel) {
    NavHost(navController, startDestination = Screen.Inventory.route) {
        composable(Screen.Inventory.route) { ScreenContent("Инвентаризация") }

        composable(Screen.Placement.route){
            SearchScreen(
                navController = navController,
                scannerViewModel = scannerViewModel,
                spHelper = spHelper
            ) {}
        }
        
        // Экран снятия товара
        composable(Screen.Removal.route) {
            val pickViewModel = hiltViewModel<PickViewModel>()
            SnyatieScanLocationScreen(
                navController = navController, 
                scannerViewModel = scannerViewModel, 
                pickViewModel = pickViewModel, 
                spHelper = spHelper
            )
        }
        
        // Экран перемещения товара
        composable(Screen.Movement.route) { 
            ProductIdScreen(navController = navController, scannerViewModel = scannerViewModel, spHelper = spHelper)
        }
        
        composable(Screen.Info.route) { 
            ProductSearchScreen(navController = navController, scannerViewModel = scannerViewModel)
        }

        composable("search_results/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            UnitSelectionScreen(navController, productId =  query, spHelper)
        }

        composable("product_info") { 
            val placementViewModel = hiltViewModel<PlacementViewModel>()
            ProductInfoScreen(navController, spHelper, placementViewModel) 
        }
        
        composable("scan_ir_location") { 
            ScanLocationScreen(navController, spHelper, scannerViewModel) 
        }
        
        composable("scan_buffer_location") { 
            val placementViewModel = hiltViewModel<PlacementViewModel>()
            ScanBufferLocationScreen(navController, spHelper, scannerViewModel, placementViewModel) 
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
        
        // Экраны для перемещения товара
        composable("product_id_input") { 
            ProductIdScreen(navController = navController, scannerViewModel = scannerViewModel, spHelper = spHelper)
        }
        composable("scan_source_location") { 
            SourceLocationScreen(navController = navController, scannerViewModel = scannerViewModel, spHelper = spHelper)
        }
        composable("quantity_input") { 
            QuantityInputScreen(navController = navController, spHelper = spHelper)
        }
        composable("scan_target_location") { 
            TargetLocationScreen(navController = navController, scannerViewModel = scannerViewModel, spHelper = spHelper)
        }
        composable("confirmation") { 
            ConfirmationScreen(navController = navController, spHelper = spHelper)
        }

        // Экран поиска товаров
        composable("product_search") {
            ProductSearchScreen(navController = navController, scannerViewModel = scannerViewModel)
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

@Composable
fun StartScreen(
    navController: NavController,
    spHelper: SPHelper,
    scannerViewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.navigate("scan_storage_location") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text("Снятие")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("placement") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text("Размещение")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("movement") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text("Перемещение")
        }
    }
}
