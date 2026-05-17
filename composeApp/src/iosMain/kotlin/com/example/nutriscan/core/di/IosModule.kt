package com.example.nutriscan.core.di

import com.example.nutriscan.core.util.DatabaseDriverFactory
import com.example.nutriscan.data.local.datastore.DataStoreFactory
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
    single { DataStoreFactory() }
}

fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}
