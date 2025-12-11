package org.society.appname.authentication.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.usecase.LoginUseCase
import org.society.appname.authentication.presentation.state.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.society.appname.authentication.domain.usecase.AppleSignInUseCase
import org.society.appname.authentication.domain.usecase.GoogleSignInUseCase
import org.society.appname.authentication.providers.ProviderSignInResult

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val appleSignInUseCase: AppleSignInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onLoginClicked(
        providerSignInResult: ProviderSignInResult? = null,
    ) {
        val currentState = _uiState.value

        if (currentState.isLoading) return

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )
        println("viewModelScope is active : ${viewModelScope.isActive}")
        viewModelScope.launch {
            val result = when (providerSignInResult) {
                is ProviderSignInResult.GoogleAuthResult -> {
                    googleSignInUseCase(providerSignInResult.idToken)
                }

                is ProviderSignInResult.AppleAuthResult -> {
                    appleSignInUseCase(providerSignInResult.idToken, providerSignInResult.rawNonce, providerSignInResult.fullName)
                }

                else -> loginUseCase(
                    currentState.email,
                    currentState.password
                )
            }
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        errorMessage = null
                    )
                }

                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = false,
                        errorMessage =  "Login failed"
                    )
                }
                null -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google login failed with null result"
                )
            }
        }
    }

    fun resetLoginSuccess() {
        _loginSuccess.value = false
    }
}