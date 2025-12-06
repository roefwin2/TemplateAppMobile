package org.society.appname.module.authentication.domain.usecase

import org.society.appname.module.authentication.AuthResult
import org.society.appname.module.authentication.domain.repository.AuthRepository

class LogoutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(): AuthResult<Unit> = repo.logout()
}