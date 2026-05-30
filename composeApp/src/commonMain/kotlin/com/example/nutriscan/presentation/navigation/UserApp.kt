package com.example.nutriscan.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.nutriscan.presentation.screens.consultation.ChatScreen
import com.example.nutriscan.presentation.screens.consultation.ConsultationScreen
import com.example.nutriscan.presentation.screens.history.HistoryScreen
import com.example.nutriscan.presentation.screens.home.HomeScreen
import com.example.nutriscan.presentation.screens.profile.ProfileScreen
import com.example.nutriscan.presentation.screens.result.ResultScreen
import com.example.nutriscan.presentation.screens.scan.ScanScreen

/**
 * The main application shell for a logged-in USER:
 * a bottom navigation bar (Beranda · Riwayat · [Scan] · Konsultasi · Profil)
 * with a raised central Scan button, plus stacked Scan/Result/Chat destinations.
 */
@Composable
fun UserApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentName = routeNameOf(backStackEntry?.destination?.route)
    val isMainTab = currentName in UserTabs.map { it.name }

    Scaffold(
        bottomBar = {
            if (isMainTab) {
                NutriBottomBar(
                    tabs = UserTabs,
                    currentRouteName = currentName,
                    onSelect = { navController.navigateTab(it) },
                    withCenterGap = true,
                    onScanClick = { navController.navigate(Route.Scan) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Home> {
                HomeScreen(
                    onScanClick = { navController.navigate(Route.Scan) },
                    onOpenResult = { barcode -> navController.navigate(Route.Result(barcode)) },
                    onSeeAllHistory = { navController.navigateTab(Route.History) },
                    onOpenConsultation = { navController.navigateTab(Route.Consultation) }
                )
            }

            composable<Route.History> {
                HistoryScreen(
                    onScanSelected = { barcode -> navController.navigate(Route.Result(barcode)) }
                )
            }

            composable<Route.Consultation> {
                ConsultationScreen(
                    onOpenChat = { conversationId -> navController.navigate(Route.Chat(conversationId)) }
                )
            }

            composable<Route.Profile> {
                ProfileScreen()
            }

            composable<Route.Scan> {
                ScanScreen(
                    onClose = { navController.popBackStack() },
                    onDetected = { barcode ->
                        navController.navigate(Route.Result(barcode)) {
                            popUpTo(Route.Scan) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Result> { entry ->
                val result: Route.Result = entry.toRoute()
                ResultScreen(
                    barcode = result.barcode,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Route.Chat> { entry ->
                val chat: Route.Chat = entry.toRoute()
                ChatScreen(
                    conversationId = chat.conversationId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Standard bottom-tab navigation: single-top + state restoration. */
fun NavHostController.navigateTab(route: Route) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
