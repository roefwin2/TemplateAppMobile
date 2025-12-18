package com.example.app.feature.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.feature.onboarding.domain.model.OnboardingDraft
import com.example.app.feature.onboarding.domain.model.OnboardingStep

/**
 * Displays the content of a single onboarding step.
 */
@Composable
fun OnboardingStepContent(
    step: OnboardingStep,
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    profileImageUri: String,
    onProfileImageChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    summaryDraft: OnboardingDraft,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (step) {
            OnboardingStep.Welcome -> WelcomeStep(
                firstName = firstName,
                lastName = lastName,
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange
            )

            OnboardingStep.Preferences -> PreferencesStep(
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                notificationsEnabled = notificationsEnabled,
                onNotificationsChange = onNotificationsChange,
                language = language,
                onLanguageChange = onLanguageChange
            )

            OnboardingStep.Profile -> ProfileStep(
                profileImageUri = profileImageUri,
                onProfileImageChange = onProfileImageChange,
                bio = bio,
                onBioChange = onBioChange
            )

            OnboardingStep.Summary -> SummaryStep(draft = summaryDraft)
        }
    }
}

@Composable
private fun WelcomeStep(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit
) {
    Text(
        text = "Welcome",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Let's start with your name so we can personalize your experience.",
        style = MaterialTheme.typography.bodyMedium
    )
    OutlinedTextField(
        value = firstName,
        onValueChange = onFirstNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("First name") }
    )
    OutlinedTextField(
        value = lastName,
        onValueChange = onLastNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Last name") }
    )
}

@Composable
private fun PreferencesStep(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit
) {
    Text(
        text = "Preferences",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Choose how you want the app to behave.",
        style = MaterialTheme.typography.bodyMedium
    )

    PreferenceToggle(
        title = "Dark mode",
        description = "Use a darker palette to reduce eye strain.",
        checked = darkMode,
        onCheckedChange = onDarkModeChange
    )
    PreferenceToggle(
        title = "Notifications",
        description = "Receive updates and reminders.",
        checked = notificationsEnabled,
        onCheckedChange = onNotificationsChange
    )
    OutlinedTextField(
        value = language,
        onValueChange = onLanguageChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Language") },
        placeholder = { Text("en, fr, es...") }
    )
}

@Composable
private fun PreferenceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProfileStep(
    profileImageUri: String,
    onProfileImageChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit
) {
    Text(
        text = "Profile",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Add a personal touch. You can always update this later.",
        style = MaterialTheme.typography.bodyMedium
    )
    OutlinedTextField(
        value = profileImageUri,
        onValueChange = onProfileImageChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Profile image URI (optional)") },
        placeholder = { Text("content://... or https://...") }
    )
    OutlinedTextField(
        value = bio,
        onValueChange = onBioChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        label = { Text("Short bio") },
        placeholder = { Text("Tell us a bit about yourself") }
    )
    Text(
        text = "${bio.length}/180",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.align(Alignment.End)
    )
}

@Composable
private fun SummaryStep(draft: OnboardingDraft) {
    Text(
        text = "Summary",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Review your information before finishing.",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(4.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryLine(label = "Name", value = "${draft.firstName.orEmpty()} ${draft.lastName.orEmpty()}".trim())
            HorizontalDivider()
            val prefs = draft.preferences
            SummaryLine(
                label = "Theme",
                value = if (prefs?.darkMode == true) "Dark" else "Light"
            )
            SummaryLine(
                label = "Notifications",
                value = if (prefs?.notificationsEnabled == false) "Disabled" else "Enabled"
            )
            SummaryLine(label = "Language", value = prefs?.language.orEmpty())
            HorizontalDivider()
            SummaryLine(label = "Bio", value = draft.bio.orEmpty().ifBlank { "No bio yet" })
            SummaryLine(
                label = "Profile image",
                value = draft.profileImageUri?.takeIf { it.isNotBlank() } ?: "Not provided"
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
