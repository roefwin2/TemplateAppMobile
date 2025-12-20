package org.society.appname.authentication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository

/**
 * État de la session utilisateur
 */
sealed class SessionState {
    data object Loading : SessionState()
    data object Unauthenticated : SessionState()
    data class NeedsOnboarding(val user: User) : SessionState()
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
                .catch {
                    _sessionState.value = SessionState.Unauthenticated
                }
                .collect { user ->
                    if (user == null) {
                        _sessionState.value = SessionState.Unauthenticated
                    } else {
                        _sessionState.value = SessionState.Loading
                        val completed = authRepository.isOnboardingCompleted(user.uid)
                        _sessionState.value =
                            if (completed) SessionState.Authenticated(user)
                            else SessionState.NeedsOnboarding(user)
                    }
                }
        }
    }

    fun onOnboardingCompleted() {
        val currentUser = (sessionState.value as? SessionState.NeedsOnboarding)?.user
        if (currentUser != null) {
            _sessionState.value = SessionState.Authenticated(currentUser)
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

    

    /**
     * Sauvegarder le token FCM
     */
    fun saveToken(newToken: String) {
        viewModelScope.launch {
            val result = authRepository.saveFcmToken(newToken)
            println("✅ Token FCM reçu: $result")
        }
    }
override fun onCleared() {
        super.onCleared()
        authObserverJob?.cancel()
    }
}