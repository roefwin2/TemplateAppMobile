package org.society.appname.authentication.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.society.appname.authentication.User
import org.society.appname.testing.FakeAuthRepository

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun logoutFlowUpdatesState() {
        val viewModel = SettingsViewModel(FakeAuthRepository())
        val user = User(uid = "user-1", email = "regis@gmail.com", displayName = "Regis")

        composeTestRule.setContent {
            SettingsScreen(
                user = user,
                onNavigateBack = {},
                onLogoutSuccess = {},
                onAccountDeleted = {},
                viewModel = viewModel
            )
        }

        // Attendre que le bouton existe dans l'arbre sémantique
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Se déconnecter")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Scroller jusqu'au bouton et cliquer
        composeTestRule.onNodeWithText("Se déconnecter")
            .performScrollTo()
            .performClick()

        // Attendre et cliquer sur le bouton de confirmation
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Déconnexion")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Déconnexion")
            .performClick()

        // Vérifier que l'état est mis à jour
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertTrue(viewModel.uiState.value.isLoggedOut)
        }
    }
}
