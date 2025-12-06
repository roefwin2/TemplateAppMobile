package org.society.appname.module.authentication.presentation.state

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val number: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)