package org.society.appname.module.authentication.domain.usecase

import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.domain.repository.AuthRepository

class SaveTokenUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(token: String): AuthResult<Unit> = repo.saveFcmToken(token)
}