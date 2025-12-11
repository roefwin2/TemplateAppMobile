package org.society.appname.authentication.domain.usecase

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository

class GoogleSignInUseCase(private val authRepository: AuthRepository){
    suspend operator fun invoke(idToken: String): AuthResult<User> {
        return authRepository.signInWithGoogleIdToken(idToken)
    }
}