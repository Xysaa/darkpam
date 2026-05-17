package com.example.nutriscan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.presentation.screens.history.HistoryScreen
import com.example.nutriscan.presentation.screens.home.HomeScreen
import com.example.nutriscan.presentation.screens.onboarding.OnboardingScreen
import com.example.nutriscan.presentation.screens.profile.ProfileScreen
import com.example.nutriscan.presentation.screens.result.ResultScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // ── Onboarding gate ───────────────────────────────────────────────────────
    // Read the onboarding flag from DataStore and redirect accordingly.
    val userPrefs: UserPreferences = koinInject()
    val isOnboardingDone by userPrefs.isOnboardingCompleted.collectAsStateWithLifecycle(false)

    LaunchedEffect(isOnboardingDone) {
        if (isOnboardingDone) {
            // If we are still sitting on Onboarding, push to Home
            val current = navController.currentBackStackEntry?.destination?.route
            if (current?.contains("Onboarding") == true) {
                navController.navigate(Route.Home) {
                    popUpTo(Route.Onboarding) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = if (isOnboardingDone) Route.Home else Route.Onboarding,
        modifier         = modifier
    ) {

        // ── Onboarding ────────────────────────────────────────────────────────
        composable<Route.Onboarding> {
            OnboardingScreen(
                onProfileSaved = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // ── Home / Scanner ────────────────────────────────────────────────────
        composable<Route.Home> {
            HomeScreen(
                onBarcodeScanned = { barcode ->
                    navController.navigate(Route.Result(barcode))
                },
                onNavigateToHistory = {
                    navController.navigate(Route.History)
                },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                }
            )
        }

        // ── Result ────────────────────────────────────────────────────────────
        composable<Route.Result> { backStackEntry ->
            val route: Route.Result = backStackEntry.toRoute()
            ResultScreen(
                barcode        = route.barcode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate(Route.Profile) }
            )
        }

        // ── History ───────────────────────────────────────────────────────────
        composable<Route.History> {
            HistoryScreen(
                onNavigateBack   = { navController.popBackStack() },
                onScanSelected   = { barcode ->
                    navController.navigate(Route.Result(barcode))
                }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable<Route.Profile> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
