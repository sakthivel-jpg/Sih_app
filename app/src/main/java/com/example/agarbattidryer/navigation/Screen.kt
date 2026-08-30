package com.example.agarbattidryer.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "HOME",
        icon = Icons.Rounded.Home
    )

    data object History : Screen(
        route = "history",
        title = "HISTORY",
        icon = Icons.Rounded.History
    )
    data object More : Screen(
        route = "more",
        title = "MORE",
        icon = Icons.Rounded.MoreHoriz
    )

    data object Connect : Screen(
        route = "connect",
        title = "CONNECT",
        icon = Icons.Rounded.MoreHoriz
    )

    data object QrMain : Screen(
        route = "qr_main",
        title = "QR",
        icon = Icons.Rounded.QrCodeScanner // Or QrCode
    )

    data object QrScanner : Screen(
        route = "qr_scanner",
        title = "SCAN QR",
        icon = Icons.Rounded.QrCodeScanner
    )

    data object WifiProvisioning : Screen(
        route = "wifi_provisioning/{deviceId}",
        title = "PROVISIONING",
        icon = Icons.Rounded.MoreHoriz
    ) {
        fun createRoute(deviceId: String) = "wifi_provisioning/$deviceId"
    }

    companion object {
        val bottomNavItems = listOf(Home, History, QrMain, More)
    }
}
