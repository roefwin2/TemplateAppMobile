package org.society.appname.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.society.appname.authentication.AuthResult
import org.society.appname.onboarding.data.local.OnboardingLocalDataSource
import org.society.appname.onboarding.domain.model.OnboardingConfig
import org.society.appname.onboarding.domain.model.OnboardingDraft
import org.society.appname.onboarding.domain.model.OnboardingStepConfig
import org.society.appname.onboarding.domain.usecases.CompleteOnboardingUseCase

/**
 * État UI de l'onboarding
 */
data class OnboardingUiState(
    val currentStepIndex: Int = 0,
    val totalSteps: Int = OnboardingConfig.totalSteps,
    val currentStep: OnboardingStepConfig? = OnboardingConfig.getStep(0),
    val draft: OnboardingDraft = OnboardingDraft(),

    // États de chargement et erreurs
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Erreurs de validation par champ
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val stepError: String? = null
) {
    val progress: Float
        get() = if (totalSteps > 0) (currentStepIndex + 1).toFloat() / totalSteps.toFloat() else 0f

    val canGoBack: Boolean
        get() = currentStepIndex > 0

    val isLastStep: Boolean
        get() = currentStepIndex == totalSteps - 1

    val isRegistrationStep: Boolean
        get() = currentStep is OnboardingStepConfig.Registration
}

/**
 * ViewModel pour l'onboarding
 *
 * Utilise CompleteOnboardingUseCase pour:
 * - Créer le compte
 * - Sauvegarder les préférences dans Firestore
 * - Marquer l'onboarding comme complété
 */
class OnboardingViewModel(
    private val localDataSource: OnboardingLocalDataSource,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    init {
        observeDraft()
    }

    private fun observeDraft() {
        viewModelScope.launch {
            localDataSource.draft.collect { draft ->
                _uiState.update { state ->
                    state.copy(
                        currentStepIndex = draft.currentStepIndex,
                        currentStep = OnboardingConfig.getStep(draft.currentStepIndex),
                        draft = draft
                    )
                }
            }
        }
    }

    // ===== Navigation =====

    fun nextStep() {
        val currentState = _uiState.value

        if (!validateCurrentStep()) {
            return
        }

        // Si c'est le step d'inscription, lancer l'onboarding complet
        if (currentState.currentStep is OnboardingStepConfig.Registration) {
            completeOnboarding()
            return
        }

        // Si c'est le dernier step (summary), terminer
        if (currentState.isLastStep) {
            _registrationSuccess.value = true
            return
        }

        localDataSource.nextStep()
        clearErrors()
    }

    fun previousStep() {
        localDataSource.previousStep()
        clearErrors()
    }

    fun goToStep(index: Int) {
        localDataSource.setCurrentStep(index)
        clearErrors()
    }

    // ===== Champs d'inscription =====

    fun onDisplayNameChange(name: String) {
        localDataSource.updateDisplayName(name)
        _uiState.update { it.copy(displayNameError = null, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        localDataSource.updateEmail(email)
        _uiState.update { it.copy(emailError = null, errorMessage = null) }
    }

    fun onPhoneNumberChange(phone: String) {
        localDataSource.updatePhoneNumber(phone)
    }

    fun onPasswordChange(password: String) {
        localDataSource.updatePassword(password)
        val draft = localDataSource.draft.value
        _uiState.update {
            it.copy(
                passwordError = null,
                confirmPasswordError = if (draft.confirmPassword.isNotEmpty() &&
                    draft.confirmPassword != password
                ) "Les mots de passe ne correspondent pas" else null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        localDataSource.updateConfirmPassword(confirmPassword)
        val draft = localDataSource.draft.value
        _uiState.update {
            it.copy(
                confirmPasswordError = if (confirmPassword != draft.password)
                    "Les mots de passe ne correspondent pas" else null,
                errorMessage = null
            )
        }
    }

    // ===== Réponses aux questions =====

    fun onSingleChoiceSelected(stepId: String, optionId: String) {
        localDataSource.saveSingleChoice(stepId, optionId)
        _uiState.update { it.copy(stepError = null) }
    }

    fun onMultiChoiceToggled(stepId: String, optionId: String, maxSelections: Int? = null) {
        localDataSource.toggleMultiChoice(stepId, optionId, maxSelections)
        _uiState.update { it.copy(stepError = null) }
    }

    fun onTextInputChange(stepId: String, text: String) {
        localDataSource.saveTextAnswer(stepId, text)
        _uiState.update { it.copy(stepError = null) }
    }

    // ===== Validation =====

    private fun validateCurrentStep(): Boolean {
        val state = _uiState.value
        val draft = state.draft
        val step = state.currentStep ?: return true

        return when (step) {
            is OnboardingStepConfig.Intro -> true
            is OnboardingStepConfig.Summary -> true

            is OnboardingStepConfig.SingleChoice -> {
                val answer = draft.getSingleChoiceAnswer(step.id)
                if (step.required && answer == null) {
                    _uiState.update { it.copy(stepError = "Veuillez sélectionner une option") }
                    false
                } else true
            }

            is OnboardingStepConfig.MultiChoice -> {
                val answers = draft.getMultiChoiceAnswers(step.id)
                when {
                    answers.size < step.minSelections -> {
                        _uiState.update {
                            it.copy(stepError = "Sélectionnez au moins ${step.minSelections} option(s)")
                        }
                        false
                    }

                    step.maxSelections != null && answers.size > step.maxSelections -> {
                        _uiState.update {
                            it.copy(stepError = "Maximum ${step.maxSelections} options")
                        }
                        false
                    }

                    else -> true
                }
            }

            is OnboardingStepConfig.MultiChoiceGrouped -> {
                val answers = draft.getMultiChoiceAnswers(step.id)
                if (answers.size < step.minSelections) {
                    _uiState.update {
                        it.copy(stepError = "Sélectionnez au moins ${step.minSelections} option(s)")
                    }
                    false
                } else true
            }

            is OnboardingStepConfig.TextInput -> {
                val answer = draft.getTextAnswer(step.id)
                if (answer.isBlank()) {
                    _uiState.update { it.copy(stepError = "Ce champ est requis") }
                    false
                } else true
            }

            is OnboardingStepConfig.TextInputOptional -> true

            is OnboardingStepConfig.Registration -> validateRegistrationFields()
        }
    }

    private fun validateRegistrationFields(): Boolean {
        val draft = _uiState.value.draft
        var isValid = true

        // Nom
        if (draft.displayName.isBlank()) {
            _uiState.update { it.copy(displayNameError = "Le nom est requis") }
            isValid = false
        } else if (draft.displayName.length < 2) {
            _uiState.update { it.copy(displayNameError = "Minimum 2 caractères") }
            isValid = false
        }

        // Email
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        when {
            draft.email.isBlank() -> {
                _uiState.update { it.copy(emailError = "L'email est requis") }
                isValid = false
            }

            !draft.email.matches(emailRegex) -> {
                _uiState.update { it.copy(emailError = "Email invalide") }
                isValid = false
            }
        }

        // Password
        val passwordErrors = mutableListOf<String>()
        if (draft.password.length < 8) passwordErrors.add("8 caractères")
        if (!draft.password.any { it.isUpperCase() }) passwordErrors.add("1 majuscule")
        if (!draft.password.any { it.isDigit() }) passwordErrors.add("1 chiffre")

        if (passwordErrors.isNotEmpty()) {
            _uiState.update {
                it.copy(passwordError = "Requis: ${passwordErrors.joinToString(", ")}")
            }
            isValid = false
        }

        // Confirm password
        if (draft.confirmPassword != draft.password) {
            _uiState.update {
                it.copy(confirmPasswordError = "Les mots de passe ne correspondent pas")
            }
            isValid = false
        }

        return isValid
    }

    // ===== Onboarding Completion =====

    /**
     * Lance le processus complet d'onboarding:
     * 1. Création du compte Firebase
     * 2. Sauvegarde des préférences dans Firestore
     * 3. Sauvegarde des réponses
     * 4. Marquage onboarding complété
     */
    private fun completeOnboarding() {
        if (!validateRegistrationFields()) return

        val draft = _uiState.value.draft

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = completeOnboardingUseCase(draft)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Passer au step suivant (summary)
                    localDataSource.nextStep()
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapErrorMessage(result.exception)
                        )
                    }
                }
            }
        }
    }

    private fun mapErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("email", ignoreCase = true) == true &&
                    exception.message?.contains("already", ignoreCase = true) == true ->
                "Cette adresse email est déjà utilisée"

            exception.message?.contains("network", ignoreCase = true) == true ->
                "Erreur de connexion. Vérifiez votre connexion internet."

            exception.message?.contains("weak", ignoreCase = true) == true ->
                "Le mot de passe est trop faible"

            else -> exception.message ?: "Une erreur est survenue"
        }
    }

    private fun clearErrors() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                displayNameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                stepError = null
            )
        }
    }

    fun resetRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    fun reset() {
        localDataSource.reset()
        _uiState.value = OnboardingUiState()
    }
}