package org.society.appname.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    private val authStateFlow = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): AuthResult<User> {
        return if (email == "regis@gmail.com" && password == "123456") {
            AuthResult.Success(User(uid = "test-user", email = email))
        } else {
            AuthResult.Error(IllegalArgumentException("Invalid credentials"))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        number: String
    ): AuthResult<User> {
        return AuthResult.Success(
            User(uid = "registered-user", email = email, displayName = displayName, number = number)
        )
    }

    override suspend fun isOnboardingCompleted(uid: String): Boolean = true

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult<User> {
        return AuthResult.Error(IllegalStateException("Not used in tests"))
    }

    override suspend fun signInWithAppleIdToken(
        idToken: String,
        rawNonce: String,
        fullName: String?
    ): AuthResult<User> {
        return AuthResult.Error(IllegalStateException("Not used in tests"))
    }

    override suspend fun logout(): AuthResult<Unit> = AuthResult.Success(Unit)

    override suspend fun deleteAccount(): AuthResult<Unit> = AuthResult.Success(Unit)

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> = AuthResult.Success(Unit)

    override fun observeAuthState(): Flow<User?> = authStateFlow

    override suspend fun saveFcmToken(token: String): AuthResult<Unit> = AuthResult.Success(Unit)

    override suspend fun saveUserData(data: Map<String, Any?>): AuthResult<Unit> =
        AuthResult.Success(Unit)

    override suspend fun saveOnboardingPreferences(preferences: Map<String, Any?>): AuthResult<Unit> =
        AuthResult.Success(Unit)

    override suspend fun markOnboardingCompleted(): AuthResult<Unit> = AuthResult.Success(Unit)
}
