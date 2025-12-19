package org.society.appname.onboarding.domain.model

/**
 * Types de steps d'onboarding supportés
 */
enum class OnboardingStepType {
    INTRO,
    SINGLE_CHOICE,
    MULTI_CHOICE,
    TEXT_INPUT,
    TEXT_INPUT_OPTIONAL,
    MULTI_CHOICE_GROUPED,
    REGISTRATION,
    SUMMARY
}

/**
 * Configuration d'un step d'onboarding
 */
sealed class OnboardingStepConfig(
    open val id: String,
    open val type: OnboardingStepType
) {
    /**
     * Step d'introduction / contexte
     */
    data class Intro(
        override val id: String,
        val title: String,
        val description: String,
        val emoji: String? = null,
        val ctaLabel: String = "C'est parti !"
    ) : OnboardingStepConfig(id, OnboardingStepType.INTRO)

    /**
     * Question à choix unique
     */
    data class SingleChoice(
        override val id: String,
        val question: String,
        val description: String? = null,
        val options: List<ChoiceOption>,
        val required: Boolean = true
    ) : OnboardingStepConfig(id, OnboardingStepType.SINGLE_CHOICE)

    /**
     * Question à choix multiples
     */
    data class MultiChoice(
        override val id: String,
        val question: String,
        val description: String? = null,
        val options: List<ChoiceOption>,
        val minSelections: Int = 1,
        val maxSelections: Int? = null
    ) : OnboardingStepConfig(id, OnboardingStepType.MULTI_CHOICE)

    /**
     * Question avec saisie texte obligatoire
     */
    data class TextInput(
        override val id: String,
        val question: String,
        val description: String? = null,
        val placeholder: String = "",
        val maxLength: Int? = 200
    ) : OnboardingStepConfig(id, OnboardingStepType.TEXT_INPUT)

    /**
     * Question avec saisie texte optionnelle
     */
    data class TextInputOptional(
        override val id: String,
        val question: String,
        val description: String? = null,
        val placeholder: String = "",
        val maxLength: Int? = 200
    ) : OnboardingStepConfig(id, OnboardingStepType.TEXT_INPUT_OPTIONAL)

    /**
     * Choix multiples groupés par catégorie
     */
    data class MultiChoiceGrouped(
        override val id: String,
        val question: String,
        val description: String? = null,
        val groups: List<ChoiceGroup>,
        val minSelections: Int = 1,
        val maxSelections: Int? = null
    ) : OnboardingStepConfig(id, OnboardingStepType.MULTI_CHOICE_GROUPED)

    /**
     * Step d'inscription (nom, email, password)
     */
    data class Registration(
        override val id: String,
        val title: String = "Créez votre compte",
        val description: String = "Pour sauvegarder vos préférences"
    ) : OnboardingStepConfig(id, OnboardingStepType.REGISTRATION)

    /**
     * Step de résumé / finalisation
     */
    data class Summary(
        override val id: String,
        val title: String = "Tout est prêt !",
        val description: String = "Votre profil a été créé avec succès",
        val ctaLabel: String = "Commencer"
    ) : OnboardingStepConfig(id, OnboardingStepType.SUMMARY)
}

/**
 * Option de choix simple
 */
data class ChoiceOption(
    val id: String,
    val label: String,
    val emoji: String? = null,
    val description: String? = null
)

/**
 * Groupe d'options pour les choix groupés
 */
data class ChoiceGroup(
    val title: String,
    val options: List<ChoiceOption>
)

/**
 * Réponse à un step d'onboarding
 */
sealed class OnboardingAnswer(
    open val stepId: String
) {
    data class SingleChoiceAnswer(
        override val stepId: String,
        val selectedOptionId: String
    ) : OnboardingAnswer(stepId)

    data class MultiChoiceAnswer(
        override val stepId: String,
        val selectedOptionIds: List<String>
    ) : OnboardingAnswer(stepId)

    data class TextAnswer(
        override val stepId: String,
        val text: String
    ) : OnboardingAnswer(stepId)
}