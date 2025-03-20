package com.komus.sorage_mobile.presentation.screens.razmechenie

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.navigation.NavRoutes
import com.komus.sorage_mobile.util.NetworkUtils
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PlacementRouterScreen(
    navController: NavController,
    networkUtils: NetworkUtils = hiltViewModel()
) {
    var isOnline by rememberSaveable { mutableStateOf(networkUtils.isNetworkAvailable()) }

    // Наблюдаем за состоянием сети
    LaunchedEffect(Unit) {
        networkUtils.observeNetworkState().collectLatest { isNetworkAvailable ->
            isOnline = isNetworkAvailable
        }
    }
    LaunchedEffect(isOnline) {
        if (isOnline) {
            navController.navigate(NavRoutes.ONLINE_PLACEMENT) {
                popUpTo(NavRoutes.PLACEMENT_ROUTER) { inclusive = true }
            }
        } else {
            navController.navigate(NavRoutes.OFFLINE_PLACEMENT) {  // Make sure the correct route is used
                popUpTo(NavRoutes.PLACEMENT_ROUTER) { inclusive = true }
            }
        }
    }
} 