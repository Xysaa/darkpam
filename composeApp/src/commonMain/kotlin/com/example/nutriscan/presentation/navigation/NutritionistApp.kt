package com.example.nutriscan.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
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
import com.example.nutriscan.presentation.screens.consultation.NutritionistDashboardScreen
import com.example.nutriscan.presentation.screens.profile.ProfileScreen

/**
 * Application shell for a logged-in NUTRITIONIST ("ahli gizi"):
 * a 2-tab bottom bar (Konsultasi · Profil) plus the shared Chat destination.
 */
@Composable
fun NutritionistApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentName = routeNameOf(backStackEntry?.destination?.route)
    val isMainTab = currentName in NutritionistTabs.map { it.name }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isMainTab) {
                NutriBottomBar(
                    tabs = NutritionistTabs,
                    currentRouteName = currentName,
                    onSelect = { navController.navigateTab(it) },
                    withCenterGap = false
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.NutritionistHome,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.NutritionistHome> {
                NutritionistDashboardScreen(
                    onOpenChat = { conversationId -> navController.navigate(Route.Chat(conversationId)) }
                )
            }

            composable<Route.Profile> {
                ProfileScreen()
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
