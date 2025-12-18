package com.example.app.feature.onboarding.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.feature.onboarding.domain.model.OnboardingStep
import com.example.app.feature.onboarding.presentation.components.OnboardingPager
import com.example.app.feature.onboarding.presentation.components.OnboardingProgressIndicator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Compose Multiplatform onboarding screen using an horizontal pager.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var darkMode by rememberSaveable { mutableStateOf(false) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var language by rememberSaveable { mutableStateOf("en") }
    var profileImageUri by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.draft) {
        firstName = state.draft.firstName.orEmpty()
        lastName = state.draft.lastName.orEmpty()
        state.draft.preferences?.let { prefs ->
            darkMode = prefs.darkMode
            notificationsEnabled = prefs.notificationsEnabled
            language = prefs.language
        }
        profileImageUri = state.draft.profileImageUri.orEmpty()
        bio = state.draft.bio.orEmpty()
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OnboardingProgressIndicator(
                stepCount = state.stepCount,
                currentStepIndex = state.currentStep.ordinal
            )

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(8.dp))

            OnboardingPager(
                state = state,
                firstName = firstName,
                lastName = lastName,
                onFirstNameChange = { firstName = it },
                onLastNameChange = { lastName = it },
                darkMode = darkMode,
                notificationsEnabled = notificationsEnabled,
                language = language,
                onDarkModeChange = { darkMode = it },
                onNotificationsChange = { notificationsEnabled = it },
                onLanguageChange = { language = it },
                profileImageUri = profileImageUri,
                bio = bio,
                onProfileImageChange = { profileImageUri = it },
                onBioChange = { bio = it },
                onPrevious = { viewModel.onEvent(OnboardingEvent.Previous) },
                onNext = {
                    handleNext(
                        state.currentStep,
                        viewModel,
                        firstName,
                        lastName,
                        darkMode,
                        notificationsEnabled,
                        language,
                        profileImageUri,
                        bio
                    )
                },
                onComplete = { viewModel.onEvent(OnboardingEvent.Complete) },
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.onEvent(OnboardingEvent.ClearError) }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }

    state.completedData?.let { data ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(OnboardingEvent.ClearError) },
            title = { Text("Onboarding completed") },
            text = {
                Text("Thanks ${data.firstName}, your onboarding is finished.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(OnboardingEvent.ClearError) }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun handleNext(
    currentStep: OnboardingStep,
    viewModel: OnboardingViewModel,
    firstName: String,
    lastName: String,
    darkMode: Boolean,
    notificationsEnabled: Boolean,
    language: String,
    profileImageUri: String,
    bio: String
) {
    when (currentStep) {
        OnboardingStep.Welcome -> viewModel.onEvent(
            OnboardingEvent.SubmitWelcome(firstName = firstName, lastName = lastName)
        )

        OnboardingStep.Preferences -> viewModel.onEvent(
            OnboardingEvent.SubmitPreferences(
                darkMode = darkMode,
                notificationsEnabled = notificationsEnabled,
                language = language
            )
        )

        OnboardingStep.Profile -> viewModel.onEvent(
            OnboardingEvent.SubmitProfile(
                profileImageUri = profileImageUri,
                bio = bio
            )
        )

        OnboardingStep.Summary -> viewModel.onEvent(OnboardingEvent.Complete)
    }
}
