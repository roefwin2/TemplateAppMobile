package org.society.appname.onboarding.domain

import org.society.appname.onboarding.domain.model.OnboardingData

/**
 * Contract used to forward validated onboarding data to the authentication feature.
 */
interface OnboardingCompletionHandler {
    /**
     * Called when the onboarding flow is fully completed.
     */
    suspend fun onOnboardingCompleted(data: OnboardingData)
}
