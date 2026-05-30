package com.example.nutriscan.data.repository

import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.domain.repository.SessionRepository
import com.example.nutriscan.domain.repository.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SessionRepositoryImpl(
    private val prefs: UserPreferences
) : SessionRepository {

    override val state: Flow<SessionState> = combine(
        prefs.isLoggedIn,
        prefs.userRole,
        prefs.displayName,
        prefs.coins,
        prefs.isOnboardingCompleted
    ) { loggedIn, role, name, coins, onboarding ->
        SessionState(
            isLoggedIn          = loggedIn,
            role                = UserRole.fromString(role),
            userName            = name,
            coins               = coins,
            onboardingCompleted = onboarding
        )
    }

    override val coins: Flow<Int> = prefs.coins

    override suspend fun login(role: UserRole, name: String) =
        prefs.login(role.name, name)

    override suspend fun logout() = prefs.logout()

    override suspend fun topUp(amount: Int) = prefs.addCoins(amount)

    override suspend fun trySpend(amount: Int): Boolean = prefs.trySpendCoins(amount)
}
