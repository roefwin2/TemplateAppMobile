package org.society.appname.onboarding.domain.model

/**
 * Configuration complète du flow d'onboarding
 *
 * Modifiez cette liste pour personnaliser votre onboarding
 */
object OnboardingConfig {

    val steps: List<OnboardingStepConfig> = listOf(

        // ===== STEP 1: Introduction =====
        OnboardingStepConfig.Intro(
            id = "intro",
            title = "Bienvenue !",
            description = "Construisons ensemble votre profil pour une expérience personnalisée",
            emoji = "👋",
            ctaLabel = "C'est parti !"
        ),

        // ===== STEP 2: Expérience dans le domaine =====
        OnboardingStepConfig.SingleChoice(
            id = "experience",
            question = "Depuis combien de temps êtes-vous dans le domaine Food & Beverage ?",
            options = listOf(
                ChoiceOption("newcomer", "Je débute", "🌱"),
                ChoiceOption("1_3_years", "1-3 ans", "🌿"),
                ChoiceOption("3_5_years", "3-5 ans", "🌳"),
                ChoiceOption("5_10_years", "5-10 ans", "🏆"),
                ChoiceOption("10_plus", "10+ ans", "👑")
            )
        ),

        // ===== STEP 3: Cuisines préférées =====
        OnboardingStepConfig.MultiChoice(
            id = "cuisines",
            question = "Quelles cuisines vous passionnent ?",
            description = "Sélectionnez toutes celles qui vous inspirent",
            options = listOf(
                ChoiceOption("french", "Française", "🇫🇷"),
                ChoiceOption("italian", "Italienne", "🇮🇹"),
                ChoiceOption("japanese", "Japonaise", "🇯🇵"),
                ChoiceOption("mexican", "Mexicaine", "🇲🇽"),
                ChoiceOption("indian", "Indienne", "🇮🇳"),
                ChoiceOption("thai", "Thaïlandaise", "🇹🇭"),
                ChoiceOption("chinese", "Chinoise", "🇨🇳"),
                ChoiceOption("mediterranean", "Méditerranéenne", "🫒"),
                ChoiceOption("american", "Américaine", "🇺🇸"),
                ChoiceOption("korean", "Coréenne", "🇰🇷"),
                ChoiceOption("vietnamese", "Vietnamienne", "🇻🇳"),
                ChoiceOption("african", "Africaine", "🌍")
            ),
            minSelections = 1,
            maxSelections = 5
        ),

        // ===== STEP 4: Plat préféré =====
        OnboardingStepConfig.TextInput(
            id = "favorite_dish",
            question = "Quel est votre plat signature ?",
            description = "Celui que vous adorez préparer ou déguster",
            placeholder = "Ex: Risotto aux champignons..."
        ),

        // ===== STEP 5: Personnalité =====
        OnboardingStepConfig.MultiChoice(
            id = "personality",
            question = "Qu'est-ce qui vous correspond le plus ?",
            description = "Choisissez jusqu'à 3 traits",
            options = listOf(
                ChoiceOption("creative", "Créatif", "🎨"),
                ChoiceOption("perfectionist", "Perfectionniste", "✨"),
                ChoiceOption("adventurous", "Aventurier", "🧭"),
                ChoiceOption("traditional", "Traditionnel", "📜"),
                ChoiceOption("innovative", "Innovant", "💡"),
                ChoiceOption("social", "Social", "🤝"),
                ChoiceOption("methodical", "Méthodique", "📊"),
                ChoiceOption("spontaneous", "Spontané", "⚡")
            ),
            minSelections = 1,
            maxSelections = 3
        ),

        // ===== STEP 6: Centres d'intérêt =====
        OnboardingStepConfig.MultiChoiceGrouped(
            id = "interests",
            question = "Vos centres d'intérêt ?",
            description = "Sélectionnez ce qui vous passionne",
            groups = listOf(
                ChoiceGroup(
                    title = "🍳 Cuisine",
                    options = listOf(
                        ChoiceOption("recipes", "Recettes"),
                        ChoiceOption("techniques", "Techniques"),
                        ChoiceOption("ingredients", "Ingrédients"),
                        ChoiceOption("plating", "Dressage")
                    )
                ),
                ChoiceGroup(
                    title = "🍷 Boissons",
                    options = listOf(
                        ChoiceOption("wine", "Vins"),
                        ChoiceOption("cocktails", "Cocktails"),
                        ChoiceOption("coffee", "Café"),
                        ChoiceOption("tea", "Thé")
                    )
                ),
                ChoiceGroup(
                    title = "💼 Business",
                    options = listOf(
                        ChoiceOption("management", "Management"),
                        ChoiceOption("marketing", "Marketing"),
                        ChoiceOption("finance", "Finance"),
                        ChoiceOption("events", "Événements")
                    )
                )
            ),
            minSelections = 2
        ),

        // ===== STEP 7: Boisson signature (optionnel) =====
        OnboardingStepConfig.TextInputOptional(
            id = "signature_drink",
            question = "Votre boisson signature ?",
            description = "Optionnel - Un cocktail, un vin, un café...",
            placeholder = "Ex: Espresso Martini..."
        ),

        // ===== STEP 8: Inscription =====
        OnboardingStepConfig.Registration(
            id = "registration",
            title = "Créez votre compte",
            description = "Pour sauvegarder votre profil personnalisé"
        ),

        // ===== STEP 9: Résumé =====
        OnboardingStepConfig.Summary(
            id = "summary",
            title = "Votre profil est prêt ! 🎉",
            description = "Découvrez du contenu adapté à vos goûts",
            ctaLabel = "Commencer l'aventure"
        )
    )

    val totalSteps: Int = steps.size

    fun getStep(index: Int): OnboardingStepConfig? = steps.getOrNull(index)

    fun getStepById(id: String): OnboardingStepConfig? = steps.find { it.id == id }

    fun getStepIndex(id: String): Int = steps.indexOfFirst { it.id == id }
}