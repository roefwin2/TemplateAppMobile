package org.society.appname.module.authentication.domain.usecase

import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.User
import org.society.appname.module.authentication.domain.repository.AuthRepository

class RegisterUseCase(
    private val repo: AuthRepository,
    private val saveTokenUseCase: SaveTokenUseCase
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
        number: String
    ): AuthResult<User> {
        val result = repo.register(email, password, displayName, number)
        if (result is AuthResult.Success) {
            // Optionnel : sauvegarder le token FCM après inscription
            // saveTokenUseCase(currentToken)
        }
        return result
    }
}