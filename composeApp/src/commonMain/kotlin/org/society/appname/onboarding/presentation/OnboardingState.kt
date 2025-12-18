package org.society.appname.onboarding.presentation

import org.society.appname.onboarding.domain.model.OnboardingData
import org.society.appname.onboarding.domain.model.OnboardingDraft
import org.society.appname.onboarding.domain.model.OnboardingStep

/**
 * UI state for the onboarding flow.
 */
data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val draft: OnboardingDraft = OnboardingDraft(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val completedData: OnboardingData? = null
) {
    val stepCount: Int = OnboardingStep.count
}
