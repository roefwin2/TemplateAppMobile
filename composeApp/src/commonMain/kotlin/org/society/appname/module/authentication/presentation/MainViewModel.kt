package org.society.appname.module.authentication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.society.appname.module.authentication.User
import org.society.appname.module.authentication.domain.repository.AuthRepository

/**
 * État de la session utilisateur
 */
sealed class SessionState {
    data object Loading : SessionState()
    data object Unauthenticated : SessionState()
    data class Authenticated(val user: User) : SessionState()
}

/**
 * ViewModel principal qui gère la session utilisateur
 */
class MainViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var authObserverJob: Job? = null

    init {
        observeAuthState()
    }

    /**
     * Observer les changements d'authentification
     */
    private fun observeAuthState() {
        authObserverJob?.cancel()

        authObserverJob = viewModelScope.launch {
            authRepository.observeAuthState()
                .catch { exception ->
                    println("⚠️ Erreur dans observeAuthState: ${exception.message}")
                    _sessionState.value = SessionState.Unauthenticated
                }
                .collect { user ->
                    _sessionState.value = if (user != null) {
                        SessionState.Authenticated(user)
                    } else {
                        SessionState.Unauthenticated
                    }
                }
        }
    }

    /**
     * Déconnexion
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authObserverJob?.cancel()
                authObserverJob = null

                _sessionState.value = SessionState.Loading

                authRepository.logout()

                observeAuthState()
            } catch (e: Exception) {
                println("❌ Erreur lors de la déconnexion: ${e.message}")
                _sessionState.value = SessionState.Unauthenticated
                observeAuthState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authObserverJob?.cancel()
    }
}