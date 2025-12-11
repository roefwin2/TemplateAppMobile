package org.society.appname.authentication.providers

import org.society.appname.authentication.providers.ProviderSignInResult.AppleAuthResult
import org.society.appname.authentication.providers.ProviderSignInResult.GoogleAuthResult

interface SocialAuthManager {
    /**
     * Configure le client Google Sign-In
     * Retourne un Intent à lancer depuis l'Activity
     */
    fun prepareGoogleSignIn(): Any? // Intent sur Android, null sur iOS

    /**
     * Traite le résultat du Google Sign-In
     * Appelé après le retour de l'Activity
     */
    suspend fun handleGoogleSignInResult(data: Any?): Result<GoogleAuthResult>

    suspend fun signInWithApple(): Result<AppleAuthResult>
    suspend fun signOut()
}

sealed class ProviderSignInResult {
    data class GoogleAuthResult(
        val idToken: String
    ) : ProviderSignInResult()

    data class AppleAuthResult(
        val idToken: String,
        val rawNonce: String,
        val fullName: String? = null,
    ) : ProviderSignInResult()
}