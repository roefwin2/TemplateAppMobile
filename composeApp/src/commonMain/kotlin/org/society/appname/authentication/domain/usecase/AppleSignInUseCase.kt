package org.society.appname.authentication.domain.usecase

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository

class AppleSignInUseCase(private val authRepository: AuthRepository){
    suspend operator fun invoke(idToken: String, rawNonce: String,fullName: String?): AuthResult<User> {
        return authRepository.signInWithAppleIdToken(idToken, rawNonce,fullName)
    }
}