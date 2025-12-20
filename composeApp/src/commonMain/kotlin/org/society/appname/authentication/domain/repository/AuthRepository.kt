package org.society.appname.authentication.domain.repository

import kotlinx.coroutines.flow.Flow
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User

interface AuthRepository {

    // ===== Authentication =====
    suspend fun login(email: String, password: String): AuthResult<User>

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        number: String
    ): AuthResult<User>

    suspend fun isOnboardingCompleted(uid: String): Boolean

    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult<User>

    suspend fun signInWithAppleIdToken(
        idToken: String,
        rawNonce: String,
        fullName: String?
    ): AuthResult<User>

    suspend fun logout(): AuthResult<Unit>

    suspend fun deleteAccount(): AuthResult<Unit>

    // ===== Password =====
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>

    // ===== User Data =====
    fun observeAuthState(): Flow<User?>

    suspend fun saveFcmToken(token: String): AuthResult<Unit>

    /**
     * Sauvegarde des données génériques dans le document utilisateur Firestore
     * Les données sont mergées avec les données existantes.
     *
     * @param data Map de données à sauvegarder
     * @return AuthResult<Unit>
     */
    suspend fun saveUserData(data: Map<String, Any?>): AuthResult<Unit>

    /**
     * Sauvegarde les préférences d'onboarding dans un sous-document "preferences"
     *
     * @param preferences Map des préférences collectées pendant l'onboarding
     * @return AuthResult<Unit>
     */
    suspend fun saveOnboardingPreferences(preferences: Map<String, Any?>): AuthResult<Unit>

    /**
     * Marque l'onboarding comme complété pour l'utilisateur actuel
     */
    suspend fun markOnboardingCompleted(): AuthResult<Unit>
}