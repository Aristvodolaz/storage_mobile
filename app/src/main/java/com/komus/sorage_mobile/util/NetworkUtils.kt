package com.komus.sorage_mobile.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Состояние сети одно на приложение. Обновляется системным событием (NetworkCallback),
 * а не опросом каждые несколько секунд: UI узнаёт о смене сети сразу и без лишней работы.
 */
@Singleton
class NetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    private val _online = MutableStateFlow(isNetworkAvailable())

    /** Есть ли сетевой канал с доступом в интернет (INTERNET); сигнал обновляется системой. */
    val online: StateFlow<Boolean> = _online.asStateFlow()

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _online.value = isNetworkAvailable()
            }

            override fun onLost(network: Network) {
                _online.value = isNetworkAvailable()
            }
        })
    }

    /**
     * Синхронная проверка для решений «сейчас или в очередь».
     * Не требует подтверждения интернета: сервер склада может быть в локальной сети без выхода в интернет.
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        return connectivityManager.getNetworkCapabilities(network) != null
    }

    fun observeNetworkState(): StateFlow<Boolean> = online
}
