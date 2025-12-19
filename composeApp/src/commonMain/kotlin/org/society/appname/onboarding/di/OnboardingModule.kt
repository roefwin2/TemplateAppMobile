package org.society.appname.onboarding.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.society.appname.onboarding.data.local.OnboardingLocalDataSource
import org.society.appname.onboarding.domain.OnboardingCompletionHandler
import org.society.appname.onboarding.domain.model.OnboardingData
import org.society.appname.onboarding.presentation.OnboardingViewModel

/**
 * Koin module for the onboarding feature.
 */
val onboardingModule = module {
    // Data
    singleOf(::OnboardingLocalDataSource)
    // Communication with authentication
    single<OnboardingCompletionHandler> { LoggingOnboardingCompletionHandler() }

    // Presentation
    viewModelOf(::OnboardingViewModel)
}

private class LoggingOnboardingCompletionHandler : OnboardingCompletionHandler {
    override suspend fun onOnboardingCompleted(data: OnboardingData) {
        println("Onboarding completed for ${data.displayName} ${data.email}")
    }
}
