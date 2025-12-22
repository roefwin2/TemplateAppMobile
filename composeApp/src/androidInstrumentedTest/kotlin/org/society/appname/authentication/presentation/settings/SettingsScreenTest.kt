package org.society.appname.authentication.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.society.appname.authentication.User
import org.society.appname.testing.FakeAuthRepository
import kotlin.test.assertTrue

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun logoutFlowUpdatesState() {
        val viewModel = SettingsViewModel(FakeAuthRepository())
        val user = User(uid = "user-1", email = "regis@gmail.com", displayName = "Regis")

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SettingsScreen(
                user = user,
                onNavigateBack = {},
                onLogoutSuccess = {},
                onAccountDeleted = {},
                viewModel = viewModel
            )
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithText("Se déconnecter").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Déconnexion").assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertTrue(viewModel.uiState.value.isLoggedOut)
        }
    }
}
