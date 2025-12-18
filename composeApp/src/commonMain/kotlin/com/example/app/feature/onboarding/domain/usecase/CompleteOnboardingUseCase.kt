package com.example.app.feature.onboarding.domain.usecase

import com.example.app.feature.onboarding.domain.OnboardingCompletionHandler
import com.example.app.feature.onboarding.domain.model.OnboardingData
import com.example.app.feature.onboarding.domain.model.OnboardingDraft
import com.example.app.feature.onboarding.domain.model.OnboardingStep
import com.example.app.feature.onboarding.domain.repository.OnboardingRepository

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
