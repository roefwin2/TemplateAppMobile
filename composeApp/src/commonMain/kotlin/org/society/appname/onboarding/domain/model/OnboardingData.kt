package org.society.appname.onboarding.domain.model

/**
 * Draft de l'onboarding en cours - stocke toutes les données collectées
 */
data class OnboardingDraft(
    // ===== Champs d'inscription =====
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // ===== Réponses aux questions =====
    val answers: Map<String, OnboardingAnswer> = emptyMap(),

    // ===== Navigation =====
    val currentStepIndex: Int = 0
) {
    // ===== Helpers pour récupérer les réponses =====

    fun getAnswer(stepId: String): OnboardingAnswer? = answers[stepId]

    fun getSingleChoiceAnswer(stepId: String): String? =
        (answers[stepId] as? OnboardingAnswer.SingleChoiceAnswer)?.selectedOptionId

    fun getMultiChoiceAnswers(stepId: String): List<String> =
        (answers[stepId] as? OnboardingAnswer.MultiChoiceAnswer)?.selectedOptionIds ?: emptyList()

    fun getTextAnswer(stepId: String): String =
        (answers[stepId] as? OnboardingAnswer.TextAnswer)?.text ?: ""

    // ===== Validation =====

    val isRegistrationValid: Boolean
        get() = displayName.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                password == confirmPassword &&
                password.length >= 8

    // ===== Conversion vers données finales =====

    fun toOnboardingData(): OnboardingData? {
        if (!isRegistrationValid) return null
        return OnboardingData(
            displayName = displayName.trim(),
            email = email.trim(),
            phoneNumber = phoneNumber.trim().takeIf { it.isNotEmpty() },
            answers = answers,
            preferences = extractPreferences()
        )
    }

    /**
     * Extrait les préférences des réponses collectées
     */
    private fun extractPreferences(): UserPreferences {
        return UserPreferences(
            favoriteCuisines = getMultiChoiceAnswers("cuisines"),
            experienceLevel = getSingleChoiceAnswer("experience"),
            interests = getMultiChoiceAnswers("interests"),
            favoriteDish = getTextAnswer("favorite_dish").takeIf { it.isNotEmpty() },
            signatureDrink = getTextAnswer("signature_drink").takeIf { it.isNotEmpty() },
            personality = getMultiChoiceAnswers("personality")
        )
    }

    // ===== Copie avec mise à jour des réponses =====

    fun withAnswer(answer: OnboardingAnswer): OnboardingDraft {
        return copy(answers = answers + (answer.stepId to answer))
    }

    fun withSingleChoice(stepId: String, optionId: String): OnboardingDraft {
        return withAnswer(OnboardingAnswer.SingleChoiceAnswer(stepId, optionId))
    }

    fun withMultiChoice(stepId: String, optionIds: List<String>): OnboardingDraft {
        return withAnswer(OnboardingAnswer.MultiChoiceAnswer(stepId, optionIds))
    }

    fun withText(stepId: String, text: String): OnboardingDraft {
        return withAnswer(OnboardingAnswer.TextAnswer(stepId, text))
    }

    fun toggleMultiChoice(stepId: String, optionId: String, maxSelections: Int? = null): OnboardingDraft {
        val currentSelections = getMultiChoiceAnswers(stepId).toMutableList()

        if (currentSelections.contains(optionId)) {
            currentSelections.remove(optionId)
        } else {
            // Vérifier la limite max
            if (maxSelections != null && currentSelections.size >= maxSelections) {
                // Remplacer le premier élément
                currentSelections.removeAt(0)
            }
            currentSelections.add(optionId)
        }

        return withMultiChoice(stepId, currentSelections)
    }
}

/**
 * Données finales de l'onboarding après validation
 */
data class OnboardingData(
    val displayName: String,
    val email: String,
    val phoneNumber: String? = null,
    val answers: Map<String, OnboardingAnswer> = emptyMap(),
    val preferences: UserPreferences = UserPreferences()
)

/**
 * Préférences utilisateur extraites des réponses
 */
data class UserPreferences(
    val favoriteCuisines: List<String> = emptyList(),
    val experienceLevel: String? = null,
    val interests: List<String> = emptyList(),
    val favoriteDish: String? = null,
    val signatureDrink: String? = null,
    val personality: List<String> = emptyList(),
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val language: String = "fr"
)