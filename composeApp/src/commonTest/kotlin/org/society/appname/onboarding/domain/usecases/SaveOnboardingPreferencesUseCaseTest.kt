package org.society.appname.onboarding.domain.usecases

import kotlinx.coroutines.test.runTest
import org.society.appname.onboarding.domain.model.OnboardingAnswer
import org.society.appname.testing.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SaveOnboardingPreferencesUseCaseTest {

    @Test
    fun saveAnswersSendsNormalizedPayloadToRepository() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = SaveOnboardingPreferencesUseCase(fakeRepository)

        val answers = mapOf(
            "experience" to OnboardingAnswer.SingleChoiceAnswer("experience", "beginner"),
            "cuisines" to OnboardingAnswer.MultiChoiceAnswer("cuisines", listOf("it", "jp")),
            "favorite_dish" to OnboardingAnswer.TextAnswer("favorite_dish", "ramen")
        )

        useCase.saveAnswers(answers)

        val saved = fakeRepository.savedUserData
        assertNotNull(saved)
        @Suppress("UNCHECKED_CAST")
        val answersPayload = saved["onboardingAnswers"] as Map<String, Map<String, Any?>>
        assertEquals(
            mapOf("type" to "single", "value" to "beginner"),
            answersPayload["experience"]
        )
        assertEquals(
            mapOf("type" to "multi", "values" to listOf("it", "jp")),
            answersPayload["cuisines"]
        )
        assertEquals(
            mapOf("type" to "text", "value" to "ramen"),
            answersPayload["favorite_dish"]
        )
    }
}
