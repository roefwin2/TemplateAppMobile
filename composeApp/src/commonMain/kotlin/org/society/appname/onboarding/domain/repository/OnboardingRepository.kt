package org.society.appname.onboarding.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.society.appname.onboarding.domain.model.OnboardingDraft
import org.society.appname.onboarding.domain.model.OnboardingStep
import org.society.appname.onboarding.domain.model.UserPreferences

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
