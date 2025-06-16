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
import androidx.annotation.RequiresApi
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.presentation.components.TopConnectionStatusBar
import com.komus.sorage_mobile.util.NetworkUtils
import kotlinx.coroutines.flow.collectLatest

object Routes {
    const val MOVE_PRODUCT = "move_product"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(spHelper: SPHelper,
               scannerViewModel: ScannerViewModel,
               networkUtils: NetworkUtils = hiltViewModel() ) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val showBottomBar = currentRoute !in listOf("auth")

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
                    if (showBottomBar) {
                        BottomNavigationBar(navController)
                    }

                }
            },
            // Отключаем встроенные отступы Scaffold
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            NavigationGraph(navController, paddingValues, scannerViewModel, spHelper, networkUtils)
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    scannerViewModel: ScannerViewModel,
    spHelper: SPHelper,
    networkUtils: NetworkUtils
) {

    var isOnline by rememberSaveable { mutableStateOf(networkUtils.isNetworkAvailable()) }

    LaunchedEffect(Unit) {
        networkUtils.observeNetworkState().collectLatest { isNetworkAvailable ->
            isOnline = isNetworkAvailable
        }
    }


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
            startDestination = "auth",
            modifier = Modifier.fillMaxSize()
        ) {


            composable("auth") {
              //  SearchScreen(navController,scannerViewModel, spHelper = spHelper){}
                AuthScreen(navController = navController, scannerViewModel) // Display AuthScreen first
            }
            composable(Screen.Inventory.route) {
                InventoryScreen(scannerViewModel = scannerViewModel)
            }

            composable(Screen.Placement.route) {
                 SearchScreen(navController,scannerViewModel, spHelper = spHelper){}
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
                ScanSourceLocationScreen(navController, scannerViewModel, spHelper =  spHelper)
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


            composable(Routes.MOVE_PRODUCT) {
                ScanSourceLocationScreen(
                    navController = navController,
                    scannerViewModel = scannerViewModel,
                    spHelper = spHelper
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
