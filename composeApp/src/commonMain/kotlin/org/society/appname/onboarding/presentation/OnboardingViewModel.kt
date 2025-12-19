package org.society.appname.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.usecase.RegisterUseCase

data class OnboardingUiState(
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Field-specific errors
    val displayNameError: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class OnboardingViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    // Field change handlers
    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            displayName = name,
            displayNameError = false,
            errorMessage = null
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phone,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            confirmPasswordError = if (_uiState.value.confirmPassword.isNotEmpty() &&
                _uiState.value.confirmPassword != password
            ) {
                "Les mots de passe ne correspondent pas"
            } else null,
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = if (confirmPassword != _uiState.value.password) {
                "Les mots de passe ne correspondent pas"
            } else null,
            errorMessage = null
        )
    }

    // Validation functions
    fun validateName(): Boolean {
        val name = _uiState.value.displayName.trim()
        return if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(displayNameError = true)
            false
        } else {
            _uiState.value = _uiState.value.copy(displayNameError = false)
            true
        }
    }

    fun validateEmail(): Boolean {
        val email = _uiState.value.email.trim()
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        return when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(emailError = "L'email est requis")
                false
            }

            !email.matches(emailRegex) -> {
                _uiState.value = _uiState.value.copy(emailError = "Format d'email invalide")
                false
            }

            else -> {
                _uiState.value = _uiState.value.copy(emailError = null)
                true
            }
        }
    }

    fun validatePassword(): Boolean {
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        var isValid = true

        // Password requirements
        val passwordErrors = mutableListOf<String>()
        if (password.length < 8) passwordErrors.add("8 caractères minimum")
        if (!password.any { it.isUpperCase() }) passwordErrors.add("une majuscule")
        if (!password.any { it.isLowerCase() }) passwordErrors.add("une minuscule")
        if (!password.any { it.isDigit() }) passwordErrors.add("un chiffre")

        if (passwordErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                passwordError = "Requis: ${passwordErrors.joinToString(", ")}"
            )
            isValid = false
        } else {
            _uiState.value = _uiState.value.copy(passwordError = null)
        }

        // Confirm password
        if (confirmPassword != password) {
            _uiState.value = _uiState.value.copy(
                confirmPasswordError = "Les mots de passe ne correspondent pas"
            )
            isValid = false
        } else {
            _uiState.value = _uiState.value.copy(confirmPasswordError = null)
        }

        return isValid
    }

    // Registration
    fun register() {
        val state = _uiState.value

        // Final validation
        if (!validateName() || !validateEmail() || !validatePassword()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = registerUseCase(
                email = state.email.trim(),
                password = state.password,
                displayName = state.displayName.trim(),
                number = state.phoneNumber.trim()
            )

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _registrationSuccess.value = true
                }

                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = mapErrorMessage(Exception(result.exception))
                    )
                }
            }
        }
    }

    private fun mapErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("email", ignoreCase = true) == true &&
                    exception.message?.contains("already", ignoreCase = true) == true ->
                "Cette adresse email est déjà utilisée"

            exception.message?.contains("network", ignoreCase = true) == true ->
                "Erreur de connexion. Vérifiez votre connexion internet."

            exception.message?.contains("weak", ignoreCase = true) == true ->
                "Le mot de passe est trop faible"

            else -> exception.message ?: "Une erreur est survenue. Veuillez réessayer."
        }
    }

    fun resetRegistrationSuccess() {
        _registrationSuccess.value = false
    }
}