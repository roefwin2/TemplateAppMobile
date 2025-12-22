package org.society.appname.onboarding.data.local

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingLocalDataSourceTest {

    @Test
    fun toggleMultiChoiceRespectsMaxSelections() = runTest {
        val dataSource = OnboardingLocalDataSource()

        dataSource.toggleMultiChoice(stepId = "cuisines", optionId = "it", maxSelections = 1)
        dataSource.toggleMultiChoice(stepId = "cuisines", optionId = "jp", maxSelections = 1)

        val selections = dataSource.draft.value.getMultiChoiceAnswers("cuisines")
        assertEquals(listOf("jp"), selections)
    }
}
