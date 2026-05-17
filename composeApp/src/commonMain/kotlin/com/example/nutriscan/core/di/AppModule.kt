package com.example.nutriscan.core.di

import com.example.nutriscan.core.network.HttpClientFactory
import com.example.nutriscan.core.util.DatabaseDriverFactory
import com.example.nutriscan.data.local.NutriScanDatabase
import com.example.nutriscan.data.local.datastore.DataStoreFactory
import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.data.local.datastore.create
import com.example.nutriscan.data.remote.api.GeminiService
import com.example.nutriscan.data.repository.AIRepositoryImpl
import com.example.nutriscan.data.repository.ScanHistoryRepositoryImpl
import com.example.nutriscan.data.repository.UserProfileRepositoryImpl
import com.example.nutriscan.domain.repository.AIRepository
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import com.example.nutriscan.domain.repository.UserProfileRepository
import com.example.nutriscan.domain.usecase.AnalyzeNutritionUseCase
import com.example.nutriscan.domain.usecase.DeleteUserProfileUseCase
import com.example.nutriscan.domain.usecase.GetUserProfileUseCase
import com.example.nutriscan.domain.usecase.HasUserProfileUseCase
import com.example.nutriscan.domain.usecase.SaveUserProfileUseCase
import com.example.nutriscan.domain.usecase.UpdateUserProfileUseCase
import com.example.nutriscan.presentation.screens.history.HistoryViewModel
import com.example.nutriscan.presentation.screens.home.HomeViewModel
import com.example.nutriscan.presentation.screens.onboarding.OnboardingViewModel
import com.example.nutriscan.presentation.screens.profile.ProfileViewModel
import com.example.nutriscan.presentation.screens.result.ResultViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.context.startKoin

// ==================== NETWORK MODULE ====================

val networkModule = module {
    single { HttpClientFactory.create(enableLogging = true) }
    singleOf(::GeminiService)
}

// ==================== DATABASE MODULE ====================

val databaseModule = module {
    single {
        val driverFactory: DatabaseDriverFactory = get()
        NutriScanDatabase(driverFactory.createDriver())
    }
}

// ==================== PREFERENCES MODULE ====================

val preferencesModule = module {
    single { get<DataStoreFactory>().create() }
    single { UserPreferences(get()) }
}

// ==================== REPOSITORY MODULE ====================

val repositoryModule = module {
    singleOf(::UserProfileRepositoryImpl) bind UserProfileRepository::class
    singleOf(::ScanHistoryRepositoryImpl) bind ScanHistoryRepository::class
    singleOf(::AIRepositoryImpl)          bind AIRepository::class
}

// ==================== USE CASE MODULE ====================

val useCaseModule = module {
    singleOf(::GetUserProfileUseCase)
    singleOf(::SaveUserProfileUseCase)
    singleOf(::UpdateUserProfileUseCase)
    singleOf(::DeleteUserProfileUseCase)
    singleOf(::HasUserProfileUseCase)
    singleOf(::AnalyzeNutritionUseCase)
}

// ==================== VIEWMODEL MODULE ====================

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::HistoryViewModel)
    // ResultViewModel receives barcode as parameter — use parametersOf at call site
    viewModel { params ->
        ResultViewModel(
            barcode                = params.get(),
            userProfileRepository  = get(),
            scanHistoryRepository  = get(),
            analyzeNutritionUseCase = get()
        )
    }
}

// ==================== ALL SHARED MODULES ====================

val sharedModules = listOf(
    networkModule,
    databaseModule,
    preferencesModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

// ==================== INIT FUNCTION ====================

fun initKoin(
    platformModules: List<Module> = emptyList(),
    config: KoinAppDeclaration? = null
) {
    startKoin {
        config?.invoke(this)
        modules(platformModules + sharedModules)
    }
}
