package org.society.appname.module.authentication.domain.usecase

import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.User
import org.society.appname.module.authentication.domain.repository.AuthRepository

class LoginUseCase(
    private val repo: AuthRepository,
    private val saveTokenUseCase: SaveTokenUseCase
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        val result = repo.login(email, password)
        if (result is AuthResult.Success) {
            // Optionnel : sauvegarder le token FCM après login
            // saveTokenUseCase(currentToken)
        }
        return result
    }
}