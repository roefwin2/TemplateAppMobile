package org.society.appname.onboarding.domain.usecases

import org.society.appname.authentication.AuthResult
import org.society.appname.authentication.User
import org.society.appname.authentication.domain.repository.AuthRepository
import org.society.appname.onboarding.domain.model.OnboardingAnswer
import org.society.appname.onboarding.domain.model.OnboardingDraft
import org.society.appname.onboarding.domain.model.UserPreferences

/**
 * Use case complet pour finaliser l'onboarding
 *
 * Exécute dans l'ordre:
 * 1. Création du compte (register)
 * 2. Sauvegarde des préférences utilisateur
 * 3. Sauvegarde des réponses brutes de l'onboarding
 * 4. Marque l'onboarding comme complété
 */
class CompleteOnboardingUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Complète l'onboarding avec toutes les données collectées
     *
     * @param draft Le draft contenant toutes les données de l'onboarding
     * @return AuthResult<User> avec l'utilisateur créé
     */
    suspend operator fun invoke(draft: OnboardingDraft): AuthResult<User> {
        // 1. Créer le compte utilisateur
        val registerResult = authRepository.register(
            email = draft.email.trim(),
            password = draft.password,
            displayName = draft.displayName.trim(),
            number = draft.phoneNumber.trim()
        )

        // Si l'inscription échoue, retourner l'erreur
        if (registerResult is AuthResult.Error) {
            println("⚠️ Erreur inscription: ${registerResult.exception.message}")
            return registerResult
        }

        val user = (registerResult as AuthResult.Success).data

        // 2. Sauvegarder les préférences extraites
        val preferences = extractPreferences(draft)
        val preferencesResult = authRepository.saveOnboardingPreferences(preferences.toMap())

        if (preferencesResult is AuthResult.Error) {
            println("⚠️ Erreur sauvegarde préférences: ${preferencesResult.exception.message}")
            // On continue quand même car le compte est créé
        }

        // 3. Sauvegarder les réponses brutes
        if (draft.answers.isNotEmpty()) {
            val answersResult = saveAnswers(draft.answers)
            if (answersResult is AuthResult.Error) {
                println("⚠️ Erreur sauvegarde réponses: ${answersResult.exception.message}")
            }
        }

        // 4. Marquer l'onboarding comme complété
        val completeResult = authRepository.markOnboardingCompleted()

        if (completeResult is AuthResult.Error) {
            println("⚠️ Erreur marquage onboarding: ${completeResult.exception.message}")
        }
        println("✅ Onboarding terminé avec succès")
        return AuthResult.Success(user)
    }

    /**
     * Version simplifiée pour les utilisateurs déjà authentifiés (Google/Apple)
     * qui doivent juste compléter leur profil
     */
    suspend fun completeProfile(draft: OnboardingDraft): AuthResult<Unit> {
        // 1. Sauvegarder le displayName si fourni
        if (draft.displayName.isNotBlank()) {
            authRepository.saveUserData(mapOf("displayName" to draft.displayName.trim()))
        }

        // 2. Sauvegarder les préférences
        val preferences = extractPreferences(draft)
        authRepository.saveOnboardingPreferences(preferences.toMap())

        // 3. Sauvegarder les réponses
        if (draft.answers.isNotEmpty()) {
            saveAnswers(draft.answers)
        }

        // 4. Marquer comme complété
        return authRepository.markOnboardingCompleted()
    }

    /**
     * Extrait les préférences des réponses du draft
     */
    private fun extractPreferences(draft: OnboardingDraft): UserPreferences {
        return UserPreferences(
            favoriteCuisines = draft.getMultiChoiceAnswers("cuisines"),
            experienceLevel = draft.getSingleChoiceAnswer("experience"),
            interests = draft.getMultiChoiceAnswers("interests"),
            favoriteDish = draft.getTextAnswer("favorite_dish").takeIf { it.isNotEmpty() },
            signatureDrink = draft.getTextAnswer("signature_drink").takeIf { it.isNotEmpty() },
            personality = draft.getMultiChoiceAnswers("personality")
        )
    }

    /**
     * Sauvegarde les réponses brutes dans Firestore
     */
    private suspend fun saveAnswers(answers: Map<String, OnboardingAnswer>): AuthResult<Unit> {
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