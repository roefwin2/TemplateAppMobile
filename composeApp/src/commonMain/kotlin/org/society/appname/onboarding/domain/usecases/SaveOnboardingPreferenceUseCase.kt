package org.society.appname.onboarding.domain.usecases

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.domain.repository.AuthRepository
import org.society.appname.onboarding.domain.model.OnboardingAnswer
import org.society.appname.onboarding.domain.model.UserPreferences

/**
 * Use case pour sauvegarder les préférences collectées pendant l'onboarding
 */
class SaveOnboardingPreferencesUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Sauvegarde les préférences d'onboarding dans Firestore
     *
     * @param preferences UserPreferences à sauvegarder
     * @return AuthResult<Unit>
     */
    suspend operator fun invoke(preferences: UserPreferences): AuthResult<Unit> {
        val preferencesMap = preferences.toMap()
        return authRepository.saveOnboardingPreferences(preferencesMap)
    }

    /**
     * Sauvegarde les réponses brutes de l'onboarding
     *
     * @param answers Map des réponses par stepId
     * @return AuthResult<Unit>
     */
    suspend fun saveAnswers(answers: Map<String, OnboardingAnswer>): AuthResult<Unit> {
        val answersMap = answers.mapValues { (_, answer) ->
            when (answer) {
                is OnboardingAnswer.SingleChoiceAnswer -> mapOf(
                    "type" to "single",
                    "value" to answer.selectedOptionId
                )
                is OnboardingAnswer.MultiChoiceAnswer -> mapOf(
                    "type" to "multi",
                    "values" to answer.selectedOptionIds
                )
                is OnboardingAnswer.TextAnswer -> mapOf(
                    "type" to "text",
                    "value" to answer.text
                )
            }
        }

        return authRepository.saveUserData(mapOf("onboardingAnswers" to answersMap))
    }

    /**
     * Convertit UserPreferences en Map pour Firestore
     */
    private fun UserPreferences.toMap(): Map<String, Any?> {
        return mapOf(
            "favoriteCuisines" to favoriteCuisines,
            "experienceLevel" to experienceLevel,
            "interests" to interests,
            "favoriteDish" to favoriteDish,
            "signatureDrink" to signatureDrink,
            "personality" to personality,
            "darkMode" to darkMode,
            "notificationsEnabled" to notificationsEnabled,
            "language" to language
        ).filterValues { value ->
            when (value) {
                is String? -> !value.isNullOrBlank()
                is List<*> -> value.isNotEmpty()
                else -> value != null
            }
        }
    }
}