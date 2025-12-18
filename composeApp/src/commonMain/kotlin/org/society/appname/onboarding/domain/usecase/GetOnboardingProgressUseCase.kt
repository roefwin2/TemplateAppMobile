package org.society.appname.onboarding.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.society.appname.onboarding.domain.model.OnboardingProgress
import org.society.appname.onboarding.domain.repository.OnboardingRepository

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
