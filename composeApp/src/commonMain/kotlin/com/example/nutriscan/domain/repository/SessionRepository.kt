package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated view of the (dummy) login session + wallet.
 */
data class SessionState(
    val isLoggedIn: Boolean = false,
    val role: UserRole = UserRole.USER,
    val userName: String = "",
    val coins: Int = 0,
    val onboardingCompleted: Boolean = false
)

/**
 * Manages the local dummy session: which role is logged in, the display name,
 * and the coin wallet used to pay for consultations.
 */
interface SessionRepository {

    /** Combined, observable session state. */
    val state: Flow<SessionState>

    /** Observable coin balance. */
    val coins: Flow<Int>

    /** Log in with the chosen [role] and display [name]. */
    suspend fun login(role: UserRole, name: String)

    /** Log out (keeps coins & onboarding flag). */
    suspend fun logout()

    /** Add coins to the wallet (dummy top-up). */
    suspend fun topUp(amount: Int)

    /**
     * Try to spend [amount] coins. Returns true when the balance was sufficient
     * and the deduction was applied.
     */
    suspend fun trySpend(amount: Int): Boolean
}
