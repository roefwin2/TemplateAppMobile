package org.society.appname.onboarding.domain.usecase

import com.example.app.feature.onboarding.domain.model.OnboardingStep
import com.example.app.feature.onboarding.domain.model.UserPreferences
import com.example.app.feature.onboarding.domain.repository.OnboardingRepository

/**
 * Validates and persists a single onboarding step.
 */
class SaveOnboardingStepUseCase(
    private val repository: OnboardingRepository
) {
    /**
     * Validates the provided [input] and saves it into the local repository.
     */
    suspend operator fun invoke(input: OnboardingStepInput): Result<Unit> =
        runCatching {
            when (input) {
                is OnboardingStepInput.Welcome -> handleWelcome(input)
                is OnboardingStepInput.Preferences -> handlePreferences(input)
                is OnboardingStepInput.Profile -> handleProfile(input)
                is OnboardingStepInput.NavigateTo -> handleNavigation(input)
            }
        }

    private suspend fun handleWelcome(input: OnboardingStepInput.Welcome) {
        val firstName = input.firstName.trim()
        val lastName = input.lastName.trim()

        require(firstName.length >= 2) { "First name must contain at least 2 characters." }
        require(lastName.length >= 2) { "Last name must contain at least 2 characters." }
        require(firstName.none { it.isDigit() }) { "First name cannot contain numbers." }
        require(lastName.none { it.isDigit() }) { "Last name cannot contain numbers." }

        repository.saveWelcome(firstName, lastName)
        repository.setCurrentStep(OnboardingStep.Preferences)
    }

    private suspend fun handlePreferences(input: OnboardingStepInput.Preferences) {
        val normalizedLanguage = input.language.trim()
        require(normalizedLanguage.isNotEmpty()) { "Language cannot be empty." }
        require(normalizedLanguage.length in 2..10) { "Language code looks invalid." }

        val preferences = UserPreferences(
            darkMode = input.darkMode,
            notificationsEnabled = input.notificationsEnabled,
            language = normalizedLanguage
        )
        repository.savePreferences(preferences)
        repository.setCurrentStep(OnboardingStep.Profile)
    }

    private suspend fun handleProfile(input: OnboardingStepInput.Profile) {
        val bio = input.bio?.trim()?.takeIf { it.isNotEmpty() }
        require(bio == null || bio.length <= 180) { "Bio must be 180 characters or less." }

        val imageUri = input.profileImageUri?.trim()?.takeIf { it.isNotEmpty() }
        repository.saveProfile(imageUri, bio)
        repository.setCurrentStep(OnboardingStep.Summary)
    }

    private suspend fun handleNavigation(input: OnboardingStepInput.NavigateTo) {
        val draft = repository.draft.value
        val canNavigate = when (input.step) {
            OnboardingStep.Welcome -> true
            OnboardingStep.Preferences -> draft.hasWelcomeData
            OnboardingStep.Profile -> draft.hasWelcomeData && draft.hasPreferences
            OnboardingStep.Summary -> draft.toOnboardingData() != null
        }
        require(canNavigate) { "Previous steps must be completed before navigating forward." }
        repository.setCurrentStep(input.step)
    }
}

/**
 * Inputs supported by [SaveOnboardingStepUseCase].
 */
sealed interface OnboardingStepInput {
    data class Welcome(val firstName: String, val lastName: String) : OnboardingStepInput
    data class Preferences(
        val darkMode: Boolean,
        val notificationsEnabled: Boolean,
        val language: String
    ) : OnboardingStepInput

    data class Profile(
        val profileImageUri: String?,
        val bio: String?
    ) : OnboardingStepInput

    data class NavigateTo(val step: OnboardingStep) : OnboardingStepInput
}
