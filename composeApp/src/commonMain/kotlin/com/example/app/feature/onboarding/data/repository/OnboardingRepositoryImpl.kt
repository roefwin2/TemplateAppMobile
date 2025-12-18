package com.example.app.feature.onboarding.data.repository

import com.example.app.feature.onboarding.data.local.OnboardingLocalDataSource
import com.example.app.feature.onboarding.domain.model.OnboardingDraft
import com.example.app.feature.onboarding.domain.model.OnboardingStep
import com.example.app.feature.onboarding.domain.model.UserPreferences
import com.example.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple repository implementation relying on an in-memory [OnboardingLocalDataSource].
 */
class OnboardingRepositoryImpl(
    private val localDataSource: OnboardingLocalDataSource
) : OnboardingRepository {

    override val draft: StateFlow<OnboardingDraft> = localDataSource.draft
    override val currentStep: StateFlow<OnboardingStep> = localDataSource.currentStep

    override suspend fun saveWelcome(firstName: String, lastName: String) {
        localDataSource.updateDraft { draft ->
            draft.copy(firstName = firstName, lastName = lastName)
        }
    }

    override suspend fun savePreferences(preferences: UserPreferences) {
        localDataSource.updateDraft { draft ->
            draft.copy(preferences = preferences)
        }
    }

    override suspend fun saveProfile(profileImageUri: String?, bio: String?) {
        localDataSource.updateDraft { draft ->
            draft.copy(
                profileImageUri = profileImageUri,
                bio = bio
            )
        }
    }

    override suspend fun setCurrentStep(step: OnboardingStep) {
        localDataSource.setStep(step)
    }

    override suspend fun reset() {
        localDataSource.reset()
    }
}
