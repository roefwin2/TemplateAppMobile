package org.society.appname.authentication.providers

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.society.appname.authentication.providers.ProviderSignInResult.AppleAuthResult
import org.society.appname.authentication.providers.ProviderSignInResult.GoogleAuthResult

/**
 * Implémentation Android du SocialAuthManager
 * NE DÉPEND PAS de l'Activity - peut être injecté avec Koin
 */
class SocialAuthManagerAndroid(
    private val context: Context
) : SocialAuthManager {

    private var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient? =
        null

    init {
        setupGoogleSignIn()
    }

    private fun setupGoogleSignIn() {
        try {
            val webClientId = context.resources.getString(
                context.resources.getIdentifier(
                    "default_web_client_id",
                    "string",
                    context.packageName
                )
            )

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            println("Erreur Google Sign In setup: ${e.message}")
        }
    }

    /**
     * Prépare l'Intent Google Sign-In sans avoir besoin de l'Activity
     * L'Activity/Composable lancera cet Intent
     */
    override fun prepareGoogleSignIn(): Intent? {
        return try {
            // Déconnecte d'abord pour forcer la sélection du compte
            googleSignInClient?.signOut()
            googleSignInClient?.signInIntent
        } catch (e: Exception) {
            println("Erreur preparation Google Sign In: ${e.message}")
            null
        }
    }

    /**
     * Traite le résultat après que l'Activity ait reçu le résultat
     */
    override suspend fun handleGoogleSignInResult(data: Any?): Result<GoogleAuthResult> {
        return try {
            val intent = data as? Intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                Result.success(GoogleAuthResult(idToken))
            } else {
                Result.failure(Exception("Google ID token is null"))
            }
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                12501 -> "Connexion annulée"
                10 -> "Vérifiez le certificat SHA-1"
                else -> "Échec de connexion Google: ${e.message}"
            }
            Result.failure(Exception(errorMessage, e))
        } catch (e: Exception) {
            Result.failure(Exception("Erreur lors du traitement: ${e.message}", e))
        }
    }

    override suspend fun signInWithApple(): Result<AppleAuthResult> {
        return Result.failure(Exception("Apple Sign In not available on Android"))
    }

    override suspend fun signOut() {
        try {
            googleSignInClient?.signOut()
        } catch (e: Exception) {
            println("Sign out error: ${e.message}")
        }
    }

    /**
     * Vérifie si un utilisateur est déjà connecté
     */
    fun getCurrentUser() = GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Vérifie si l'utilisateur est connecté via Google
     */
    fun isSignedInWithGoogle(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
}