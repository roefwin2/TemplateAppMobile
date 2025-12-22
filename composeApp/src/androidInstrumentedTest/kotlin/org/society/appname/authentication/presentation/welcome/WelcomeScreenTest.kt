package org.society.appname.authentication.presentation.welcome

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun getStartedClickUpdatesUiState() {
        composeTestRule.setContent {
            var actionStatus by remember { mutableStateOf("idle") }

            Column {
                WelcomeScreen(
                    onGetStarted = { actionStatus = "started" },
                    onLogin = { actionStatus = "login" }
                )
                Text(
                    text = actionStatus,
                    modifier = Modifier.testTag("welcome_action_status")
                )
            }
        }

        // Attendre que le BottomSheet soit visible
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag(WelcomeScreenTestTags.BottomSheet)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(WelcomeScreenTestTags.BottomSheet).assertIsDisplayed()
        composeTestRule.onNodeWithTag(WelcomeScreenTestTags.Title).assertTextEquals("Bienvenue")

        composeTestRule.onNodeWithTag(WelcomeScreenTestTags.GetStartedButton).performClick()
        composeTestRule.onNodeWithTag("welcome_action_status").assertTextEquals("started")
    }
}
