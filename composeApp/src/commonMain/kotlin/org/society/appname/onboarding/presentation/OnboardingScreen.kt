package org.society.appname.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.society.appname.onboarding.domain.model.OnboardingConfig
import org.society.appname.onboarding.domain.model.OnboardingStepConfig
import org.society.appname.onboarding.presentation.components.IntroStepContent
import org.society.appname.onboarding.presentation.components.MultiChoiceGroupedStepContent
import org.society.appname.onboarding.presentation.components.MultiChoiceStepContent
import org.society.appname.onboarding.presentation.components.OnboardingColors
import org.society.appname.onboarding.presentation.components.RegistrationStepContent
import org.society.appname.onboarding.presentation.components.SingleChoiceStepContent
import org.society.appname.onboarding.presentation.components.SummaryStepContent
import org.society.appname.onboarding.presentation.components.TextInputOptionalStepContent
import org.society.appname.onboarding.presentation.components.TextInputStepContent

/**
 * Écran principal de l'onboarding
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val registrationSuccess by viewModel.registrationSuccess.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = uiState.currentStepIndex
    ) { OnboardingConfig.totalSteps }

    // Synchroniser le pager avec le ViewModel
    LaunchedEffect(uiState.currentStepIndex) {
        if (pagerState.currentPage != uiState.currentStepIndex) {
            pagerState.animateScrollToPage(uiState.currentStepIndex)
        }
    }

    // Succès de l'inscription
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onComplete()
            viewModel.resetRegistrationSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingColors.Background)
    ) {
        // Background décoratif
        OnboardingBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar avec retour et progression
            OnboardingTopBar(
                currentStep = uiState.currentStepIndex,
                totalSteps = uiState.totalSteps,
                canGoBack = uiState.canGoBack,
                onBack = {
                    viewModel.previousStep()
                }
            )

            // Contenu du pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = false
            ) { page ->
                val step = OnboardingConfig.getStep(page)
                step?.let {
                    OnboardingStepContent(
                        step = it,
                        draft = uiState.draft,
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }

            // Barre du bas avec bouton
            OnboardingBottomBar(
                currentStep = uiState.currentStepIndex,
                totalSteps = uiState.totalSteps,
                currentStepConfig = uiState.currentStep,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onNext = { viewModel.nextStep() }
            )
        }
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStepConfig,
    draft: org.society.appname.onboarding.domain.model.OnboardingDraft,
    viewModel: OnboardingViewModel,
    uiState: OnboardingUiState
) {
    when (step) {
        is OnboardingStepConfig.Intro -> {
            IntroStepContent(config = step)
        }

        is OnboardingStepConfig.SingleChoice -> {
            SingleChoiceStepContent(
                config = step,
                selectedOptionId = draft.getSingleChoiceAnswer(step.id),
                onOptionSelected = { viewModel.onSingleChoiceSelected(step.id, it) },
                error = uiState.stepError
            )
        }

        is OnboardingStepConfig.MultiChoice -> {
            MultiChoiceStepContent(
                config = step,
                selectedOptionIds = draft.getMultiChoiceAnswers(step.id),
                onOptionToggled = {
                    viewModel.onMultiChoiceToggled(
                        step.id,
                        it,
                        step.maxSelections
                    )
                },
                error = uiState.stepError
            )
        }

        is OnboardingStepConfig.MultiChoiceGrouped -> {
            MultiChoiceGroupedStepContent(
                config = step,
                selectedOptionIds = draft.getMultiChoiceAnswers(step.id),
                onOptionToggled = {
                    viewModel.onMultiChoiceToggled(
                        step.id,
                        it,
                        step.maxSelections
                    )
                },
                error = uiState.stepError
            )
        }

        is OnboardingStepConfig.TextInput -> {
            TextInputStepContent(
                config = step,
                value = draft.getTextAnswer(step.id),
                onValueChange = { viewModel.onTextInputChange(step.id, it) },
                error = uiState.stepError
            )
        }

        is OnboardingStepConfig.TextInputOptional -> {
            TextInputOptionalStepContent(
                config = step,
                value = draft.getTextAnswer(step.id),
                onValueChange = { viewModel.onTextInputChange(step.id, it) }
            )
        }

        is OnboardingStepConfig.Registration -> {
            RegistrationStepContent(
                config = step,
                displayName = draft.displayName,
                email = draft.email,
                phoneNumber = draft.phoneNumber,
                password = draft.password,
                confirmPassword = draft.confirmPassword,
                onDisplayNameChange = viewModel::onDisplayNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPhoneNumberChange = viewModel::onPhoneNumberChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                displayNameError = uiState.displayNameError,
                emailError = uiState.emailError,
                passwordError = uiState.passwordError,
                confirmPasswordError = uiState.confirmPasswordError
            )
        }

        is OnboardingStepConfig.Summary -> {
            SummaryStepContent(config = step)
        }
    }
}

@Composable
private fun OnboardingBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Orbe en haut à droite
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp + offset.dp)
                .size(220.dp)
                .blur(100.dp)
                .background(
                    OnboardingColors.Primary.copy(alpha = 0.12f),
                    CircleShape
                )
        )

        // Orbe en bas à gauche
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp - offset.dp)
                .size(180.dp)
                .blur(80.dp)
                .background(
                    OnboardingColors.PrimaryLight.copy(alpha = 0.10f),
                    CircleShape
                )
        )
    }
}

@Composable
private fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int,
    canGoBack: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton retour
        AnimatedVisibility(
            visible = canGoBack,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OnboardingColors.Surface)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = OnboardingColors.TextPrimary
                )
            }
        }

        if (!canGoBack) {
            Spacer(modifier = Modifier.width(44.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Indicateur de progression
        ProgressIndicator(
            currentStep = currentStep,
            totalSteps = totalSteps
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val width by animateDpAsState(
                targetValue = if (index == currentStep) 20.dp else 6.dp,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "width"
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    OnboardingColors.Primary,
                                    OnboardingColors.PrimaryLight
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    OnboardingColors.Border,
                                    OnboardingColors.Border
                                )
                            )
                        }
                    )
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    currentStepConfig: OnboardingStepConfig?,
    isLoading: Boolean,
    errorMessage: String?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Message d'erreur
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = OnboardingColors.Error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Bouton principal
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            enabled = !isLoading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                OnboardingColors.Primary,
                                OnboardingColors.PrimaryLight
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = getButtonText(currentStepConfig, currentStep, totalSteps),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        if (currentStep < totalSteps - 1 && currentStepConfig !is OnboardingStepConfig.Registration) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getButtonText(
    step: OnboardingStepConfig?,
    currentIndex: Int,
    totalSteps: Int
): String {
    return when (step) {
        is OnboardingStepConfig.Intro -> step.ctaLabel
        is OnboardingStepConfig.Registration -> "Créer mon compte"
        is OnboardingStepConfig.Summary -> (step as? OnboardingStepConfig.Summary)?.ctaLabel
            ?: "Commencer"

        else -> if (currentIndex < totalSteps - 1) "Continuer" else "Terminer"
    }
}