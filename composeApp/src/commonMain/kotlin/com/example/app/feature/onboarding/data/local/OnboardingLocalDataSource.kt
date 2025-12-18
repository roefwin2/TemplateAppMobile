package com.example.app.feature.onboarding.data.local

import com.example.app.feature.onboarding.domain.model.OnboardingDraft
import com.example.app.feature.onboarding.domain.model.OnboardingStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory persistence for onboarding data.
 */
class OnboardingLocalDataSource {
    private val draftState = MutableStateFlow(OnboardingDraft())
    private val stepState = MutableStateFlow(OnboardingStep.Welcome)

    /**
     * Stream of the current draft.
     */
    val draft: StateFlow<OnboardingDraft> = draftState

    /**
     * Stream of the currently selected step.
     */
    val currentStep: StateFlow<OnboardingStep> = stepState

    /**
     * Applies an update to the stored draft.
     */
    fun updateDraft(update: (OnboardingDraft) -> OnboardingDraft) {
        draftState.value = update(draftState.value)
    }

    /**
     * Updates the selected step.
     */
    fun setStep(step: OnboardingStep) {
        stepState.value = step
    }

    /**
     * Clears the stored data.
     */
    fun reset() {
        draftState.value = OnboardingDraft()
        stepState.value = OnboardingStep.Welcome
    }
}
