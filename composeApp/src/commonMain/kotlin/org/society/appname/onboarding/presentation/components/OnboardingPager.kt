package org.society.appname.onboarding.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.society.appname.onboarding.domain.model.OnboardingStep
import org.society.appname.onboarding.presentation.OnboardingState

/**
 * Horizontal pager orchestrating all onboarding steps.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPager(
    state: OnboardingState,
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    darkMode: Boolean,
    notificationsEnabled: Boolean,
    language: String,
    onDarkModeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    profileImageUri: String,
    bio: String,
    onProfileImageChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentStep.ordinal,
        pageCount = { state.stepCount }
    )

    LaunchedEffect(state.currentStep) {
        pagerState.animateScrollToPage(state.currentStep.ordinal)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val step = OnboardingStep.entries[page]
            OnboardingStepContent(
                step = step,
                firstName = firstName,
                lastName = lastName,
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange,
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                notificationsEnabled = notificationsEnabled,
                onNotificationsChange = onNotificationsChange,
                language = language,
                onLanguageChange = onLanguageChange,
                profileImageUri = profileImageUri,
                onProfileImageChange = onProfileImageChange,
                bio = bio,
                onBioChange = onBioChange,
                summaryDraft = state.draft,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = state.currentStep != OnboardingStep.Welcome && !state.isLoading
            ) {
                Text("Previous")
            }

            Button(
                onClick = { if (state.currentStep == OnboardingStep.Summary) onComplete() else onNext() },
                enabled = !state.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (state.currentStep == OnboardingStep.Summary) "Finish" else "Next")
            }
        }
    }
}
