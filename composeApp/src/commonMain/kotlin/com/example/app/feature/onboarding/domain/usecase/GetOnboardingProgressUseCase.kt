package com.example.app.feature.onboarding.domain.usecase

import com.example.app.feature.onboarding.domain.model.OnboardingProgress
import com.example.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Observes onboarding progress to support resume capability.
 */
class GetOnboardingProgressUseCase(
    private val repository: OnboardingRepository
) {
    /**
     * Emits the current progress whenever either the step or the draft changes.
     */
    operator fun invoke(): Flow<OnboardingProgress> =
        combine(repository.currentStep, repository.draft) { step, draft ->
            OnboardingProgress(currentStep = step, draft = draft)
        }
}
