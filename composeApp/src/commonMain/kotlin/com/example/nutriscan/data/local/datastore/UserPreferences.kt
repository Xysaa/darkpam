package com.example.nutriscan.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User Preferences menggunakan DataStore
 * 
 * DataStore adalah pengganti SharedPreferences yang lebih modern:
 * - Asynchronous dengan Coroutines dan Flow
 * - Type-safe dengan Preferences Keys
 * - Tidak blocking main thread
 * 
 * @param dataStore Instance DataStore dari platform
 */
class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {
    // ==================== PREFERENCE KEYS ====================
    
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SORT_BY = stringPreferencesKey("sort_by")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val SHOW_PREVIEW = booleanPreferencesKey("show_preview")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // ── Session / Auth (dummy local login) ──
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ROLE    = stringPreferencesKey("user_role")     // "USER" | "NUTRITIONIST"
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val COINS        = intPreferencesKey("coins")
    }

    companion object {
        const val DEFAULT_COINS = 100
    }
    
    // ==================== DARK MODE ====================
    
    /**
     * Observe dark mode setting
     */
    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }
    
    /**
     * Set dark mode
     */
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }
    
    // ==================== SORT BY ====================
    
    /**
     * Observe sort preference
     */
    val sortBy: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.SORT_BY] ?: "UPDATED_DESC"
    }
    
    /**
     * Set sort preference
     */
    suspend fun setSortBy(sortBy: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SORT_BY] = sortBy
        }
    }
    
    // ==================== DEFAULT CATEGORY ====================
    
    /**
     * Observe default category
     */
    val defaultCategory: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_CATEGORY] ?: "GENERAL"
    }
    
    /**
     * Set default category
     */
    suspend fun setDefaultCategory(category: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CATEGORY] = category
        }
    }
    
    // ==================== SHOW PREVIEW ====================
    
    /**
     * Observe show preview setting
     */
    val showPreview: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SHOW_PREVIEW] ?: true
    }
    
    /**
     * Set show preview
     */
    suspend fun setShowPreview(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SHOW_PREVIEW] = show
        }
    }
    
    // ==================== ONBOARDING ====================
    
    /**
     * Check if onboarding completed
     */
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }
    
    /**
     * Set onboarding completed
     */
    suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    // ==================== SESSION / AUTH (dummy local login) ====================

    /** Observe whether a (dummy) user is currently logged in. */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    /** Observe the selected role: "USER" or "NUTRITIONIST". Defaults to "USER". */
    val userRole: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.USER_ROLE] ?: "USER"
    }

    /** Observe the display name chosen at login. */
    val displayName: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.DISPLAY_NAME] ?: ""
    }

    /** Observe the coin balance (defaults to [DEFAULT_COINS]). */
    val coins: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.COINS] ?: DEFAULT_COINS
    }

    /** Perform a dummy login with the chosen [role] and [name]. */
    suspend fun login(role: String, name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.USER_ROLE]    = role
            prefs[Keys.DISPLAY_NAME] = name
            // Seed coins on first login if not already present
            if (prefs[Keys.COINS] == null) {
                prefs[Keys.COINS] = DEFAULT_COINS
            }
        }
    }

    /** Clear the session (keeps coins & onboarding so re-login is seamless). */
    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }

    /** Add [amount] coins (top-up). */
    suspend fun addCoins(amount: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: DEFAULT_COINS
            prefs[Keys.COINS] = current + amount
        }
    }

    /**
     * Attempt to spend [amount] coins. Returns true if the balance was
     * sufficient and the deduction was applied, false otherwise.
     */
    suspend fun trySpendCoins(amount: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: DEFAULT_COINS
            if (current >= amount) {
                prefs[Keys.COINS] = current - amount
                success = true
            }
        }
        return success
    }
}
