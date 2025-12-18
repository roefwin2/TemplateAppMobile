package com.example.app.feature.onboarding.domain.repository

import com.example.app.feature.onboarding.domain.model.OnboardingDraft
import com.example.app.feature.onboarding.domain.model.OnboardingStep
import com.example.app.feature.onboarding.domain.model.UserPreferences
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository responsible for persisting the onboarding draft locally.
 */
interface OnboardingRepository {
    /**
     * Stream of the current onboarding draft.
     */
    val draft: StateFlow<OnboardingDraft>

    /**
     * Stream of the currently selected step.
     */
    val currentStep: StateFlow<OnboardingStep>

    /**
     * Saves the welcome information.
     */
    suspend fun saveWelcome(firstName: String, lastName: String)

    /**
     * Persists the user preferences.
     */
    suspend fun savePreferences(preferences: UserPreferences)

    /**
     * Persists profile-related fields.
     */
    suspend fun saveProfile(profileImageUri: String?, bio: String?)

    /**
     * Updates the currently selected step.
     */
    suspend fun setCurrentStep(step: OnboardingStep)

    /**
     * Resets the whole onboarding state.
     */
    suspend fun reset()
}
