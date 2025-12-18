package com.example.app.feature.onboarding.di

import com.example.app.feature.onboarding.data.local.OnboardingLocalDataSource
import com.example.app.feature.onboarding.data.repository.OnboardingRepositoryImpl
import com.example.app.feature.onboarding.domain.OnboardingCompletionHandler
import com.example.app.feature.onboarding.domain.model.OnboardingData
import com.example.app.feature.onboarding.domain.repository.OnboardingRepository
import com.example.app.feature.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.example.app.feature.onboarding.domain.usecase.GetOnboardingProgressUseCase
import com.example.app.feature.onboarding.domain.usecase.SaveOnboardingStepUseCase
import com.example.app.feature.onboarding.presentation.OnboardingViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for the onboarding feature.
 */
val onboardingModule = module {
    // Data
    singleOf(::OnboardingLocalDataSource)
    singleOf(::OnboardingRepositoryImpl) bind OnboardingRepository::class

    // Domain
    factoryOf(::SaveOnboardingStepUseCase)
    factoryOf(::GetOnboardingProgressUseCase)
    factoryOf(::CompleteOnboardingUseCase)

    // Communication with authentication
    single<OnboardingCompletionHandler> { LoggingOnboardingCompletionHandler() }

    // Presentation
    viewModelOf(::OnboardingViewModel)
}

private class LoggingOnboardingCompletionHandler : OnboardingCompletionHandler {
    override suspend fun onOnboardingCompleted(data: OnboardingData) {
        println("Onboarding completed for ${data.firstName} ${data.lastName}")
    }
}
