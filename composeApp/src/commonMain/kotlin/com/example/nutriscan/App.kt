package com.example.nutriscan

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.domain.repository.SessionRepository
import com.example.nutriscan.presentation.navigation.NutritionistApp
import com.example.nutriscan.presentation.navigation.UserApp
import com.example.nutriscan.presentation.screens.auth.LoginScreen
import com.example.nutriscan.presentation.screens.onboarding.OnboardingScreen
import com.example.nutriscan.presentation.theme.AppGradients
import com.example.nutriscan.presentation.theme.NutriScanTheme
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        val session: SessionRepository = koinInject()
        val prefs: UserPreferences = koinInject()

        val systemDark = isSystemInDarkTheme()
        val darkMode by prefs.isDarkMode.collectAsStateWithLifecycle(systemDark)
        val sessionState: com.example.nutriscan.domain.repository.SessionState? by produceState<com.example.nutriscan.domain.repository.SessionState?>(
            initialValue = null
        ) {
            session.state.collect { value = it }
        }

        NutriScanTheme(darkTheme = darkMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val state = sessionState
                when {
                    state == null -> SplashScreen()
                    !state.isLoggedIn -> LoginScreen()
                    state.role == UserRole.NUTRITIONIST -> NutritionistApp()
                    !state.onboardingCompleted -> OnboardingScreen(onProfileSaved = {})
                    else -> UserApp()
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.brandSoft),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "NutriScan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Scan. Analyze. Eat Smart.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}
