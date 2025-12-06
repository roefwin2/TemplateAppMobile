package org.society.appname.module.authentication.domain.repository

import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<User>
    suspend fun register(email: String, password: String, displayName: String, number: String): AuthResult<User>
    suspend fun saveFcmToken(token: String): AuthResult<Unit>
    suspend fun logout(): AuthResult<Unit>
    fun observeAuthState(): Flow<User?>
}