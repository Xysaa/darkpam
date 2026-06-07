package com.example.nutriscan.data.repository

import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.domain.repository.SessionRepository
import com.example.nutriscan.domain.repository.SessionState
import com.example.nutriscan.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SessionRepositoryImpl(
    private val prefs: UserPreferences,
    private val userProfileRepository: UserProfileRepository
) : SessionRepository {

    // Onboarding is considered complete only when an actual profile row exists
    // in the database. Deriving it from the DB (instead of a standalone boolean
    // flag) guarantees it can never desync — e.g. after a reinstall/restore or
    // when the user data is cleared but a stale flag remains.
    override val state: Flow<SessionState> = combine(
        prefs.isLoggedIn,
        prefs.userRole,
        prefs.displayName,
        prefs.coins,
        userProfileRepository.getProfile()
    ) { loggedIn, role, name, coins, profile ->
        SessionState(
            isLoggedIn          = loggedIn,
            role                = UserRole.fromString(role),
            userName            = name,
            coins               = coins,
            onboardingCompleted = profile != null
        )
    }

    override val coins: Flow<Int> = prefs.coins

    override suspend fun login(role: UserRole, name: String) =
        prefs.login(role.name, name)

    override suspend fun logout() = prefs.logout()

    override suspend fun topUp(amount: Int) = prefs.addCoins(amount)

    override suspend fun trySpend(amount: Int): Boolean = prefs.trySpendCoins(amount)
}
