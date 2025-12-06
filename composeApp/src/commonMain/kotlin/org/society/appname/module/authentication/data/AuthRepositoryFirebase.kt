package org.society.appname.module.authentication.data

import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.User
import org.society.appname.module.authentication.domain.repository.AuthRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AuthRepositoryFirebase(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult<User> =
        try {
            val user = auth.signInWithEmailAndPassword(email, password).user
            if (user != null) AuthResult.Success(user.toUser())
            else AuthResult.Error(Exception("Login failed"))
        } catch (t: Throwable) {
            AuthResult.Error(t)
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
                // Créer le document utilisateur dans Firestore
                val userData = mapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "displayName" to displayName,
                    "number" to number,
                    "isEmailVerified" to false
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)

                println("✅ Utilisateur enregistré avec succès")
                AuthResult.Success(
                    User(
                        uid = user.uid,
                        email = email,
                        displayName = displayName,
                        number = number,
                        isEmailVerified = false
                    )
                )
            } else {
                AuthResult.Error(Exception("Registration failed"))
            }
        } catch (t: Throwable) {
            println("❌ Erreur register: ${t.message}")
            AuthResult.Error(t)
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
}

private fun FirebaseUser.toUser() = User(
    uid = uid,
    email = email,
    displayName = displayName,
    number = phoneNumber,
    isEmailVerified = isEmailVerified
)