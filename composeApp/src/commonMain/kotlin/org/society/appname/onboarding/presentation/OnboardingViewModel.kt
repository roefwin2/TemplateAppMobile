package org.society.appname.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.society.appname.onboarding.domain.model.OnboardingStep
import org.society.appname.onboarding.domain.usecase.CompleteOnboardingUseCase
import org.society.appname.onboarding.domain.usecase.GetOnboardingProgressUseCase
import org.society.appname.onboarding.domain.usecase.OnboardingStepInput
import org.society.appname.onboarding.domain.usecase.SaveOnboardingStepUseCase

/**
 * ViewModel orchestrating onboarding UI events and state.
 */
class OnboardingViewModel(
    private val saveOnboardingStepUseCase: SaveOnboardingStepUseCase,
    private val getOnboardingProgressUseCase: GetOnboardingProgressUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        observeProgress()
    }

    /**
     * Entrypoint for UI events.
     */
    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.SubmitWelcome -> saveWelcome(event)
            is OnboardingEvent.SubmitPreferences -> savePreferences(event)
            is OnboardingEvent.SubmitProfile -> saveProfile(event)
            is OnboardingEvent.NavigateTo -> navigateTo(event.step)
            OnboardingEvent.Previous -> moveToPreviousStep()
            OnboardingEvent.Complete -> completeOnboarding()
            OnboardingEvent.ClearError -> _state.update {
                it.copy(
                    errorMessage = null,
                    completedData = null
                )
            }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            getOnboardingProgressUseCase.invoke()
                .collect { progress ->
                    _state.update {
                        it.copy(
                            currentStep = progress.currentStep,
                            draft = progress.draft
                        )
                    }
                }
        }
    }

    private fun saveWelcome(event: OnboardingEvent.SubmitWelcome) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = saveOnboardingStepUseCase(
                OnboardingStepInput.Welcome(event.firstName, event.lastName)
            )
            handleResult(result)
        }
    }

    private fun savePreferences(event: OnboardingEvent.SubmitPreferences) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = saveOnboardingStepUseCase(
                OnboardingStepInput.Preferences(
                    darkMode = event.darkMode,
                    notificationsEnabled = event.notificationsEnabled,
                    language = event.language
                )
            )
            handleResult(result)
        }
    }

    private fun saveProfile(event: OnboardingEvent.SubmitProfile) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = saveOnboardingStepUseCase(
                OnboardingStepInput.Profile(
                    profileImageUri = event.profileImageUri,
                    bio = event.bio
                )
            )
            handleResult(result)
        }
    }

    private fun navigateTo(step: OnboardingStep) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = saveOnboardingStepUseCase(OnboardingStepInput.NavigateTo(step))
            handleResult(result)
        }
    }

    private fun moveToPreviousStep() {
        val current = _state.value.currentStep
        val previous = OnboardingStep.entries
            .getOrElse(current.ordinal - 1) { OnboardingStep.Welcome }
        navigateTo(previous)
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = completeOnboardingUseCase()
            _state.update { state ->
                state.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message,
                    completedData = result.getOrNull()
                )
            }
        }
    }

    private fun handleResult(result: Result<Unit>) {
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }
}
