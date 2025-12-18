package com.example.app.feature.onboarding.domain

import com.example.app.feature.onboarding.domain.model.OnboardingData

/**
 * Contract used to forward validated onboarding data to the authentication feature.
 */
interface OnboardingCompletionHandler {
    /**
     * Called when the onboarding flow is fully completed.
     */
    suspend fun onOnboardingCompleted(data: OnboardingData)
}
