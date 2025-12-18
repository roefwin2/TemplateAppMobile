package com.example.app.feature.onboarding.presentation

import com.example.app.feature.onboarding.domain.model.OnboardingData
import com.example.app.feature.onboarding.domain.model.OnboardingDraft
import com.example.app.feature.onboarding.domain.model.OnboardingStep

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
