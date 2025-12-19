package org.society.appname.onboarding.domain.usecase

import org.society.appname.onboarding.domain.OnboardingCompletionHandler
import org.society.appname.onboarding.domain.model.OnboardingData
import org.society.appname.onboarding.domain.model.OnboardingDraft
import org.society.appname.onboarding.domain.model.OnboardingStep
import org.society.appname.onboarding.domain.repository.OnboardingRepository

/**
 * Aggregates the onboarding draft and forwards it to the authentication feature.
 */
class CompleteOnboardingUseCase(
    private val repository: OnboardingRepository,
    private val completionHandler: OnboardingCompletionHandler
) {
    /**
     * Validates completeness, notifies the handler and returns the final payload.
     */
    suspend operator fun invoke(): Result<OnboardingData> =
        runCatching {
            val draft = repository.draft.value
            val payload = buildPayload(draft)
            completionHandler.onOnboardingCompleted(payload)
            repository.setCurrentStep(OnboardingStep.Summary)
            payload
        }

    private fun buildPayload(draft: OnboardingDraft): OnboardingData =
        draft.toOnboardingData()
            ?: error("Onboarding data is incomplete.")
}
