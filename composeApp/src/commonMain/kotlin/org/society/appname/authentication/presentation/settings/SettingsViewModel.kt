package org.society.appname.authentication.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.repository.AuthRepository

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ========================================
    // DELETE ACCOUNT
    // ========================================

    fun showDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = true, deleteError = null) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = false, deleteError = null) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }

            when (val result = authRepository.deleteAccount()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            isDeleteDialogVisible = false,
                            isAccountDeleted = true
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteError = result.exception.message
                                ?: "Erreur lors de la suppression"
                        )
                    }
                }
            }
        }
    }

    // ========================================
    // LOGOUT
    // ========================================

    fun showLogoutDialog() {
        _uiState.update { it.copy(isLogoutDialogVisible = true, logoutError = null) }
    }

    fun hideLogoutDialog() {
        _uiState.update { it.copy(isLogoutDialogVisible = false, logoutError = null) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, logoutError = null) }

            when (val result = authRepository.logout()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            isLogoutDialogVisible = false,
                            isLoggedOut = true
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            logoutError = result.exception.message
                                ?: "Erreur lors de la déconnexion"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(deleteError = null, logoutError = null) }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val isLogoutDialogVisible: Boolean = false,
    val isLoggingOut: Boolean = false,
    val logoutError: String? = null,
    val isAccountDeleted: Boolean = false,
    val isLoggedOut: Boolean = false
)