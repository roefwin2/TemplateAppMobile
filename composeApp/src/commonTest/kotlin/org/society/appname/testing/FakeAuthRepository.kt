package org.society.appname.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var saveUserDataCalls: Int = 0
        private set
    var saveOnboardingPreferencesCalls: Int = 0
        private set
    var savedUserData: Map<String, Any?>? = null
        private set
    var savedOnboardingPreferences: Map<String, Any?>? = null
        private set

    private val authStateFlow = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): AuthResult<User> {
        error("Not used in tests")
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        number: String
    ): AuthResult<User> {
        error("Not used in tests")
    }

    override suspend fun isOnboardingCompleted(uid: String): Boolean {
        error("Not used in tests")
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult<User> {
        error("Not used in tests")
    }

    override suspend fun signInWithAppleIdToken(
        idToken: String,
        rawNonce: String,
        fullName: String?
    ): AuthResult<User> {
        error("Not used in tests")
    }

    override suspend fun logout(): AuthResult<Unit> {
        error("Not used in tests")
    }

    override suspend fun deleteAccount(): AuthResult<Unit> {
        error("Not used in tests")
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        error("Not used in tests")
    }

    override fun observeAuthState(): Flow<User?> = authStateFlow

    override suspend fun saveFcmToken(token: String): AuthResult<Unit> {
        error("Not used in tests")
    }

    override suspend fun saveUserData(data: Map<String, Any?>): AuthResult<Unit> {
        saveUserDataCalls += 1
        savedUserData = data
        return AuthResult.Success(Unit)
    }

    override suspend fun saveOnboardingPreferences(preferences: Map<String, Any?>): AuthResult<Unit> {
        saveOnboardingPreferencesCalls += 1
        savedOnboardingPreferences = preferences
        return AuthResult.Success(Unit)
    }

    override suspend fun markOnboardingCompleted(): AuthResult<Unit> {
        error("Not used in tests")
    }
}
