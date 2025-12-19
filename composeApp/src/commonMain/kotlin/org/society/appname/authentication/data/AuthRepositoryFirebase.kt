package org.society.appname.authentication.data

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository
import org.society.appname.authentication.providers.SocialAuthManager
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AuthRepositoryFirebase(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val socialAuthManager: SocialAuthManager
) : AuthRepository {

    private fun usersDoc(uid: String) = firestore
        .collection("users")
        .document(uid)

    private suspend fun saveUser(user: Map<String, Any?>): AuthResult<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return AuthResult.Error(Exception("No authenticated user"))
            val patch = userPatch(
                email = user["email"] as? String,
                displayName = user["displayName"] as? String,
                isEmailVerified = user["isEmailVerified"] as? Boolean,
                createdAt = (user["createdAt"] as? Number)?.toLong(),
                isOnboardingCompleted = user["isOnboardingCompleted"] as? Boolean,
                updatedAt = nowMs()
            )
            usersDoc(uid).set(patch, merge = true)
            AuthResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Error(e)
        }
    }

    private fun userPatch(
        uid: String? = null,
        email: String? = null,
        displayName: String? = null,
        isEmailVerified: Boolean? = null,
        createdAt: Long? = null,
        isOnboardingCompleted: Boolean? = null,
        updatedAt: Long? = nowMs()
    ): Map<String, Any?> {
        val m = mutableMapOf<String, Any?>()
        if (uid != null) m["uid"] = uid
        if (email != null) m["email"] = email
        if (displayName != null) m["displayName"] = displayName
        if (isEmailVerified != null) m["isEmailVerified"] = isEmailVerified
        if (createdAt != null) m["createdAt"] = createdAt
        if (isOnboardingCompleted != null) m["isOnboardingCompleted"] = isOnboardingCompleted
        if (updatedAt != null) m["updatedAt"] = updatedAt
        return m
    }

    private fun nowMs(): Long = System.now().toEpochMilliseconds()

    override suspend fun login(email: String, password: String): AuthResult<User> =
        try {
            val user = auth.signInWithEmailAndPassword(email, password).user
            if (user != null) {
                usersDoc(user.uid).set(
                    userPatch(
                        updatedAt = nowMs()
                    ),
                    merge = true
                )
                AuthResult.Success(user.toUser())
            } else {
                println("Login failed")
                AuthResult.Error(Exception("Login failed"))
            }
        } catch (t: Throwable) {
            AuthResult.Error(t)
        }

    // ========================================
    // GOOGLE SIGN IN
    // ========================================

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult<User> {
        return try {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val authResult = auth.signInWithCredential(credential)
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

                usersDoc(firebaseUser.uid).set(
                    userPatch(
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        isEmailVerified = firebaseUser.isEmailVerified,
                        createdAt = if (isNewUser) nowMs() else null,
                        isOnboardingCompleted = if (isNewUser) false else null,
                        updatedAt = nowMs()
                    ),
                    merge = true
                )

                AuthResult.Success(firebaseUser.toUser())
            } else {
                println("Firebase user is null after Google sign in")
                AuthResult.Error(Exception("Firebase user is null after Google sign in"))
            }
        } catch (e: Exception) {
            println("Google sign in error: $e")
            AuthResult.Error(e)
        }
    }

    // ========================================
    // APPLE SIGN IN
    // ========================================

    override suspend fun signInWithAppleIdToken(
        idToken: String,
        rawNonce: String,
        fullName: String?
    ): AuthResult<User> {
        return try {
            val credential = OAuthProvider.credential(
                providerId = "apple.com",
                idToken = idToken,
                rawNonce = rawNonce
            )
            val authResult = auth.signInWithCredential(credential)
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

                println("🍎 Utilisateur Firebase créé/connecté")
                println("🍎 isNewUser: $isNewUser")
                println("🍎 Firebase displayName: ${firebaseUser.displayName}")

                // Déterminer le nom à utiliser
                val displayName = when {
                    // Priorité 1: Le fullName fourni par Apple (première connexion)
                    !fullName.isNullOrBlank() -> {
                        println("🍎 Utilisation du fullName d'Apple: $fullName")
                        fullName
                    }
                    // Priorité 2: Le displayName déjà dans Firebase
                    !firebaseUser.displayName.isNullOrBlank() -> {
                        println("🍎 Utilisation du displayName Firebase: ${firebaseUser.displayName}")
                        firebaseUser.displayName
                    }
                    // Priorité 3: Extraire du email
                    else -> {
                        val emailName = firebaseUser.email?.substringBefore("@") ?: "User"
                        println("🍎 Fallback sur email: $emailName")
                        emailName
                    }
                }

                // Mettre à jour le profil Firebase si nécessaire
                if (!fullName.isNullOrBlank() && firebaseUser.displayName != fullName) {
                    try {
                        firebaseUser.updateProfile(displayName = fullName)
                        println("✅ Profile Firebase mis à jour avec: $fullName")
                    } catch (e: Exception) {
                        println("⚠️ Erreur mise à jour profil: ${e.message}")
                    }
                }

                // Sauvegarder dans Firestore
                usersDoc(firebaseUser.uid).set(
                    userPatch(
                        email = firebaseUser.email,
                        displayName = displayName,
                        isEmailVerified = true,
                        createdAt = if (isNewUser) nowMs() else null,
                        isOnboardingCompleted = if (isNewUser) false else null,
                        updatedAt = nowMs()
                    ),
                    merge = true
                )

                println("✅ Document Firestore sauvegardé avec displayName: $displayName")
                AuthResult.Success(firebaseUser.toUser())
            } else {
                AuthResult.Error(Exception("Firebase user is null after Apple sign in"))
            }
        } catch (e: Exception) {
            AuthResult.Error(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        number: String
    ): AuthResult<User> =
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password)
            val user = authResult.user

            if (user != null) {
                val patch = userPatch(
                    email = user.email,
                    displayName = displayName,
                    isEmailVerified = user.isEmailVerified,
                    createdAt = nowMs(),
                    isOnboardingCompleted = true
                )
                val res = saveUser(patch)
                res as? AuthResult.Error
                    ?: AuthResult.Success(
                        user.toUser().copy(displayName = displayName)
                    )
            } else {
                println("Registration failed FirebaseUser : $user")
                AuthResult.Error(Exception("Registration failed"))
            }
        } catch (t: Throwable) {
            println("❌ Erreur register: ${t.message}")
            AuthResult.Error(t)
        }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e)
        }
    }

    override suspend fun saveFcmToken(token: String): AuthResult<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestore
                    .collection("users")
                    .document(userId)
                    .update(mapOf("fcmToken" to token))

                println("✅ FCM token enregistré pour user: $userId")
                AuthResult.Success(Unit)
            } else {
                println("⚠️ User non connecté, token non enregistré")
                AuthResult.Error(Exception("User non connecté"))
            }
        } catch (e: Exception) {
            println("❌ Erreur sauvegarde FCM token: ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e)
        }
    }

    override suspend fun logout(): AuthResult<Unit> =
        try {
            socialAuthManager.signOut()
            auth.signOut()
            AuthResult.Success(Unit)
        } catch (t: Throwable) {
            AuthResult.Error(t)
        }

    override fun observeAuthState(): Flow<User?> =
        auth.authStateChanged
            .map { it?.toUser() }
            .catch { exception ->
                println("⚠️ Erreur dans observeAuthState: ${exception.message}")
                emit(null)
            }

    override suspend fun deleteAccount(): AuthResult<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return AuthResult.Error(Exception("No authenticated user"))

            // Utiliser NonCancellable pour garantir que toutes les opérations s'exécutent
            withContext(NonCancellable) {
                // 1. Supprimer les données Firestore de l'utilisateur
                try {
                    usersDoc(userId).delete()
                } catch (e: Exception) {
                    println("Erreur suppression document utilisateur: ${e.message}")
                }
                //TODO 3. Déconnecter l'utilisateur de RevenueCat

                // 4. Se déconnecter des providers sociaux
                try {
                    socialAuthManager.signOut()
                } catch (e: Exception) {
                    println("Erreur signOut social: ${e.message}")
                }

                // 5. Supprimer le compte Firebase Auth (en dernier)
                try {
                    auth.currentUser?.delete()
                } catch (e: Exception) {
                    println("Erreur suppression compte Firebase: ${e.message}")
                    AuthResult.Error(e)
                }
            }

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            println("Erreur lors de la suppression du compte: ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e)
        }
    }
}

private fun FirebaseUser.toUser() = User(
    uid = uid,
    email = email,
    displayName = displayName,
    number = phoneNumber,
    isEmailVerified = isEmailVerified
)