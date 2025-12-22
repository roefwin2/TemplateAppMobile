package org.society.appname.authentication.presentation.password

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.society.appname.testing.FakeAuthRepository
import kotlin.test.assertTrue

class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sendResetEmailShowsSuccessState() {
        val viewModel = ForgotPasswordViewModel(FakeAuthRepository())

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.onNodeWithTag(ForgotPasswordScreenTestTags.EmailField)
            .performTextInput("regis@gmail.com")
        composeTestRule.onNodeWithTag(ForgotPasswordScreenTestTags.SendButton).performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ForgotPasswordScreenTestTags.SuccessMessage).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(viewModel.uiState.value.isSuccess)
        }
    }
}
