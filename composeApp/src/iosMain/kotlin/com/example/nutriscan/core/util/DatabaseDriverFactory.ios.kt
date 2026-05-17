package com.example.nutriscan.core.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.nutriscan.data.local.NutriScanDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = NutriScanDatabase.Schema,
            name   = "nutriscan.db"
        )
    }
}
