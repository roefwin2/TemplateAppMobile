package org.society.appname.onboarding.domain.model

/**
 * Complete onboarding payload shared with the authentication feature once the user
 * validated every step.
 */
data class OnboardingData(
    val firstName: String,
    val lastName: String,
    val preferences: UserPreferences,
    val profileImageUri: String?,
    val bio: String?
)

/**
 * User preferences collected during onboarding.
 */
data class UserPreferences(
    val darkMode: Boolean,
    val notificationsEnabled: Boolean,
    val language: String
)

/**
 * Mutable, partial representation of the onboarding flow.
 */
data class OnboardingDraft(
    val firstName: String? = null,
    val lastName: String? = null,
    val preferences: UserPreferences? = null,
    val profileImageUri: String? = null,
    val bio: String? = null
) {
    /**
     * Returns true when required welcome information is present.
     */
    val hasWelcomeData: Boolean
        get() = !firstName.isNullOrBlank() && !lastName.isNullOrBlank()

    /**
     * Returns true when preferences were explicitly set by the user.
     */
    val hasPreferences: Boolean
        get() = preferences != null

    /**
     * Builds a final [OnboardingData] instance when all mandatory fields are filled.
     */
    fun toOnboardingData(): OnboardingData? {
        val localPreferences = preferences
        val localFirstName = firstName
        val localLastName = lastName
        if (localPreferences == null || localFirstName.isNullOrBlank() || localLastName.isNullOrBlank()) {
            return null
        }
        return OnboardingData(
            firstName = localFirstName.trim(),
            lastName = localLastName.trim(),
            preferences = localPreferences,
            profileImageUri = profileImageUri?.takeIf { it.isNotBlank() },
            bio = bio?.takeIf { it.isNotBlank() }
        )
    }
}

/**
 * Ordered steps used by the onboarding pager.
 */
enum class OnboardingStep {
    Welcome,
    Preferences,
    Profile,
    Summary;

    companion object {
        /**
         * Returns the total number of steps.
         */
        val count: Int get() = entries.size
    }
}

/**
 * Progress snapshot returned by the domain layer.
 */
data class OnboardingProgress(
    val currentStep: OnboardingStep,
    val totalSteps: Int = OnboardingStep.count,
    val draft: OnboardingDraft
)
