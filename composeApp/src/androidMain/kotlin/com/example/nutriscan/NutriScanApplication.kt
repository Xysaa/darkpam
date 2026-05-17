package com.example.nutriscan

import android.app.Application
import com.example.nutriscan.core.di.androidModule
import com.example.nutriscan.core.di.initKoin

class NutriScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            platformModules = listOf(androidModule)
        )
    }
}
