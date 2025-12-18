package org.society.appname.onboarding.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.society.appname.onboarding.data.local.OnboardingLocalDataSource
import org.society.appname.onboarding.data.repository.OnboardingRepositoryImpl
import org.society.appname.onboarding.domain.OnboardingCompletionHandler
import org.society.appname.onboarding.domain.model.OnboardingData
import org.society.appname.onboarding.domain.repository.OnboardingRepository
import org.society.appname.onboarding.domain.usecase.CompleteOnboardingUseCase
import org.society.appname.onboarding.domain.usecase.GetOnboardingProgressUseCase
import org.society.appname.onboarding.domain.usecase.SaveOnboardingStepUseCase
import org.society.appname.onboarding.presentation.OnboardingBisViewModel
import org.society.appname.onboarding.presentation.OnboardingViewModel

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
    viewModelOf(::OnboardingBisViewModel)
}

private class LoggingOnboardingCompletionHandler : OnboardingCompletionHandler {
    override suspend fun onOnboardingCompleted(data: OnboardingData) {
        println("Onboarding completed for ${data.firstName} ${data.lastName}")
    }
}
