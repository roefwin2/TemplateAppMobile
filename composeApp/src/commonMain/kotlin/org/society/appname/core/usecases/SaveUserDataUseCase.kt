package org.society.appname.core.usecases

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.repository.AuthRepository

/**
 * Use case pour sauvegarder des données génériques dans le document utilisateur Firestore
 *
 * Usage:
 * ```
 * val result = saveUserDataUseCase(
 *     mapOf(
 *         "favoriteColor" to "blue",
 *         "age" to 25,
 *         "interests" to listOf("cooking", "music")
 *     )
 * )
 * ```
 */
class SaveUserDataUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Sauvegarde des données dans le document utilisateur
     * Les données sont mergées avec les données existantes.
     *
     * @param data Map de données à sauvegarder
     * @return AuthResult<Unit>
     */
    suspend operator fun invoke(data: Map<String, Any?>): AuthResult<Unit> {
        if (data.isEmpty()) {
            return AuthResult.Success(Unit)
        }
        return authRepository.saveUserData(data)
    }
}