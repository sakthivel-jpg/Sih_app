package com.example.agarbattidryer.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
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

    companion object {
        val bottomNavItems = listOf(Home, History, More)
    }
}
