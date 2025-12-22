package org.society.appname.authentication.presentation.password

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.society.appname.testing.FakeAuthRepository

class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sendResetEmailShowsSuccessState() {
        val viewModel = ForgotPasswordViewModel(FakeAuthRepository())

        composeTestRule.setContent {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Attendre que l'animation initiale soit terminée et que le champ email soit visible
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag(ForgotPasswordScreenTestTags.EmailField)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Entrer l'email
        composeTestRule.onNodeWithTag(ForgotPasswordScreenTestTags.EmailField)
            .performTextInput("regis@gmail.com")

        // Cliquer sur le bouton d'envoi
        composeTestRule.onNodeWithTag(ForgotPasswordScreenTestTags.SendButton)
            .performClick()

        // Attendre que le message de succès apparaisse
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag(ForgotPasswordScreenTestTags.SuccessMessage)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Vérifier que le message de succès est affiché
        composeTestRule.onNodeWithTag(ForgotPasswordScreenTestTags.SuccessMessage)
            .assertIsDisplayed()

        // Vérifier l'état du ViewModel
        composeTestRule.runOnIdle {
            assertTrue(viewModel.uiState.value.isSuccess)
        }
    }
}
