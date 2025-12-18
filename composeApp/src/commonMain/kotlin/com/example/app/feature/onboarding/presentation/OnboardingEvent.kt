package com.example.app.feature.onboarding.presentation

import com.example.app.feature.onboarding.domain.model.OnboardingStep

/**
 * UI events emitted by the onboarding screen.
 */
sealed interface OnboardingEvent {
    data class SubmitWelcome(val firstName: String, val lastName: String) : OnboardingEvent
    data class SubmitPreferences(
        val darkMode: Boolean,
        val notificationsEnabled: Boolean,
        val language: String
    ) : OnboardingEvent

    data class SubmitProfile(
        val profileImageUri: String?,
        val bio: String?
    ) : OnboardingEvent

    data class NavigateTo(val step: OnboardingStep) : OnboardingEvent

    object Complete : OnboardingEvent
    object Previous : OnboardingEvent
    object ClearError : OnboardingEvent
}
