package org.society.appname.authentication.presentation.login

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.society.appname.authentication.domain.usecase.AppleSignInUseCase
import org.society.appname.authentication.domain.usecase.GoogleSignInUseCase
import org.society.appname.authentication.domain.usecase.LoginUseCase
import org.society.appname.authentication.domain.usecase.SaveTokenUseCase
import org.society.appname.testing.FakeAuthRepository
import kotlin.test.assertTrue

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loginWithValidCredentialsUpdatesState() {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(
            loginUseCase = LoginUseCase(repository, SaveTokenUseCase(repository)),
            googleSignInUseCase = GoogleSignInUseCase(repository),
            appleSignInUseCase = AppleSignInUseCase(repository)
        )

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            LoginScreen(
                onNavigateBack = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }

        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.onNodeWithTag(LoginScreenTestTags.EmailField).performTextInput("regis@gmail.com")
        composeTestRule.onNodeWithTag(LoginScreenTestTags.PasswordField).performTextInput("123456")
        composeTestRule.onNodeWithTag(LoginScreenTestTags.LoginButton)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertTrue(viewModel.uiState.value.isLoginSuccessful)
        }
    }
}
