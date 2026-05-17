package com.example.nutriscan.core.util

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.nutriscan.data.local.NutriScanDatabase

actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema  = NutriScanDatabase.Schema,
            context = context,
            name    = "nutriscan.db"
        )
    }
}
