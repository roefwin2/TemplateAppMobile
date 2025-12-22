package org.society.appname.authentication.presentation.register

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.society.appname.authentication.domain.usecase.RegisterUseCase
import org.society.appname.authentication.domain.usecase.SaveTokenUseCase
import org.society.appname.testing.FakeAuthRepository

class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun registerWithValidInputsSetsSuccess() {
        val repository = FakeAuthRepository()
        val viewModel = RegisterViewModel(
            registerUseCase = RegisterUseCase(repository, SaveTokenUseCase(repository))
        )

        composeTestRule.setContent {
            RegisterScreen(
                onNavigateToLogin = {},
                onRegisterSuccess = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(RegisterScreenTestTags.NameField).performTextInput("Regis")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.EmailField)
            .performTextInput("regis@gmail.com")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.PhoneField)
            .performTextInput("0601020304")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.PasswordField)
            .performTextInput("Test1234")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.SubmitButton).performClick()

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.registerSuccess.value
        }
        assertTrue(viewModel.registerSuccess.value)
    }
}
