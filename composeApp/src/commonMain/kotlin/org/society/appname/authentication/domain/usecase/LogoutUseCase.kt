package org.society.appname.authentication.domain.usecase

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.repository.AuthRepository

class LogoutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(): AuthResult<Unit> = repo.logout()
}