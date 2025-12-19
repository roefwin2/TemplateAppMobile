package org.society.appname.authentication.presentation.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.repository.AuthRepository

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = null
        )
    }

    fun onSendResetLinkClicked() {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.isSuccess) return

        // Validation basique de l'email
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter your email"
            )
            return
        }

        if (!isValidEmail(currentState.email)) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid email address"
            )
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(currentState.email)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }

                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to send reset email. Please check your email address."
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun reset() {
        _uiState.value = ForgotPasswordUiState()
    }
}