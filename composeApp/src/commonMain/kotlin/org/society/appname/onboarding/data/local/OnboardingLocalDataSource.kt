package org.society.appname.onboarding.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.society.appname.onboarding.domain.model.OnboardingDraft

/**
 * Stockage local en mémoire pour l'onboarding
 *
 * Simple et efficace - les données sont perdues si l'app est tuée
 * Pour une persistence plus robuste, implémenter avec DataStore ou Room
 */
class OnboardingLocalDataSource {

    private val _draft = MutableStateFlow(OnboardingDraft())
    val draft: StateFlow<OnboardingDraft> = _draft.asStateFlow()

    /**
     * Met à jour le draft avec une transformation
     */
    fun updateDraft(update: (OnboardingDraft) -> OnboardingDraft) {
        _draft.update(update)
    }

    /**
     * Remplace complètement le draft
     */
    fun setDraft(draft: OnboardingDraft) {
        _draft.value = draft
    }

    // ===== Raccourcis pour les champs d'inscription =====

    fun updateDisplayName(name: String) {
        _draft.update { it.copy(displayName = name) }
    }

    fun updateEmail(email: String) {
        _draft.update { it.copy(email = email) }
    }

    fun updatePhoneNumber(phone: String) {
        _draft.update { it.copy(phoneNumber = phone) }
    }

    fun updatePassword(password: String) {
        _draft.update { it.copy(password = password) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _draft.update { it.copy(confirmPassword = confirmPassword) }
    }

    // ===== Raccourcis pour les réponses =====

    fun saveSingleChoice(stepId: String, optionId: String) {
        _draft.update { it.withSingleChoice(stepId, optionId) }
    }

    fun saveMultiChoice(stepId: String, optionIds: List<String>) {
        _draft.update { it.withMultiChoice(stepId, optionIds) }
    }

    fun toggleMultiChoice(stepId: String, optionId: String, maxSelections: Int? = null) {
        _draft.update { it.toggleMultiChoice(stepId, optionId, maxSelections) }
    }

    fun saveTextAnswer(stepId: String, text: String) {
        _draft.update { it.withText(stepId, text) }
    }

    // ===== Navigation =====

    fun setCurrentStep(index: Int) {
        _draft.update { it.copy(currentStepIndex = index) }
    }

    fun nextStep() {
        _draft.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
    }

    fun previousStep() {
        _draft.update {
            it.copy(currentStepIndex = maxOf(0, it.currentStepIndex - 1))
        }
    }

    // ===== Reset =====

    fun reset() {
        _draft.value = OnboardingDraft()
    }
}