package org.society.appname.authentication.domain.usecase

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.repository.AuthRepository

class SaveTokenUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(token: String): AuthResult<Unit> = repo.saveFcmToken(token)
}