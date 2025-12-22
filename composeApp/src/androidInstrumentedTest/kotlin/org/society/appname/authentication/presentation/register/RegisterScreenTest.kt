package org.society.appname.authentication.presentation.register

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.society.appname.authentication.domain.usecase.RegisterUseCase
import org.society.appname.authentication.domain.usecase.SaveTokenUseCase
import org.society.appname.testing.FakeAuthRepository
import kotlin.test.assertTrue

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
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.EmailField).performTextInput("regis@gmail.com")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.PhoneField).performTextInput("0601020304")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.PasswordField).performTextInput("123456")
        composeTestRule.onNodeWithTag(RegisterScreenTestTags.SubmitButton).performClick()

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertTrue(viewModel.registerSuccess.value)
        }
    }
}
