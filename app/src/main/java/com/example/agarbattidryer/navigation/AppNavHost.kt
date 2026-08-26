package com.example.agarbattidryer.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.agarbattidryer.data.repository.DeviceRepository
import com.example.agarbattidryer.data.repository.DryingHistoryRepository
import com.example.agarbattidryer.ui.history.HistoryScreen
import com.example.agarbattidryer.ui.home.HomeScreen
import com.example.agarbattidryer.ui.home.HomeViewModel
import com.example.agarbattidryer.ui.more.MoreScreen
import com.example.agarbattidryer.ui.theme.SolarAmber
import com.example.agarbattidryer.ui.theme.SolarWarm

@Composable
fun AppNavHost(
    deviceRepository: DeviceRepository,
    historyRepository: DryingHistoryRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                val homeSelected = currentRoute == Screen.Home.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Screen.Home.icon,
                            contentDescription = Screen.Home.title,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = Screen.Home.title,
                            fontSize = 13.sp,
                            fontWeight = if (homeSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    },
                    selected = homeSelected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SolarAmber,
                        selectedTextColor = SolarAmber,
                        indicatorColor = SolarWarm,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        if (!homeSelected) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                val historySelected = currentRoute == Screen.History.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Screen.History.icon,
                            contentDescription = Screen.History.title,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = Screen.History.title,
                            fontSize = 13.sp,
                            fontWeight = if (historySelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    },
                    selected = historySelected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SolarAmber,
                        selectedTextColor = SolarAmber,
                        indicatorColor = SolarWarm,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        if (!historySelected) {
                            navController.navigate(Screen.History.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                val moreSelected = currentRoute == Screen.More.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Screen.More.icon,
                            contentDescription = Screen.More.title,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = Screen.More.title,
                            fontSize = 13.sp,
                            fontWeight = if (moreSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    },
                    selected = moreSelected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SolarAmber,
                        selectedTextColor = SolarAmber,
                        indicatorColor = SolarWarm,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        if (!moreSelected) {
                            navController.navigate(Screen.More.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(deviceRepository, historyRepository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onConnectDeviceClick = { navController.navigate(Screen.Connect.route) }
                )
            }

            composable(Screen.Connect.route) {
                // Just use the dynamic device service here
                com.example.agarbattidryer.ui.device.ConnectDeviceScreen(
                    deviceService = (deviceRepository as com.example.agarbattidryer.data.repository.DeviceRepositoryImpl).getDynamicService(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                val historyViewModel: com.example.agarbattidryer.ui.history.HistoryViewModel = viewModel(
                    factory = com.example.agarbattidryer.ui.history.HistoryViewModel.provideFactory(historyRepository)
                )
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.More.route) {
                MoreScreen()
            }
        }
    }
}