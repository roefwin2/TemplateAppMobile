package org.society.appname.module.authentication.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.domain.usecase.RegisterUseCase
import org.society.appname.module.authentication.presentation.state.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.value = _uiState.value.copy(displayName = displayName, errorMessage = null)
    }

    fun onNumberChange(number: String) {
        _uiState.value = _uiState.value.copy(number = number, errorMessage = null)
    }

    fun register() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val displayName = _uiState.value.displayName.trim()
        val number = _uiState.value.number.trim()

        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email, mot de passe et nom sont requis"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = registerUseCase(email, password, displayName, number)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _registerSuccess.value = true
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Erreur d'inscription"
                    )
                }
            }
        }
    }

    fun resetRegisterSuccess() {
        _registerSuccess.value = false
    }
}