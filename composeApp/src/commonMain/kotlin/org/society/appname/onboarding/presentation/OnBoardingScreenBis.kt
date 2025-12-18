package org.society.appname.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// Onboarding Theme
object OnboardingTheme {
    val primaryDark = Color(0xFF1A1A2E)
    val primaryAccent = Color(0xFF6366F1)
    val secondaryAccent = Color(0xFF8B5CF6)
    val surfaceCard = Color(0xFFF8F9FC)
    val surfaceInput = Color(0xFFF1F5F9)
    val textPrimary = Color(0xFF1F2937)
    val textSecondary = Color(0xFF6B7280)
    val gradientStart = Color(0xFF6366F1)
    val gradientEnd = Color(0xFFA855F7)
    val success = Color(0xFF10B981)
    val error = Color(0xFFEF4444)
}

// Onboarding steps data
data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val description: String
)

val onboardingFeatures = listOf(
    OnboardingStep(
        icon = Icons.Default.Explore,
        title = "Découvrez",
        description = "Explorez des contenus uniques et personnalisés selon vos intérêts"
    ),
    OnboardingStep(
        icon = Icons.Default.People,
        title = "Connectez",
        description = "Rejoignez une communauté de passionnés et partagez vos expériences"
    ),
    OnboardingStep(
        icon = Icons.Default.Star,
        title = "Évoluez",
        description = "Débloquez des récompenses et suivez votre progression"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingBisViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val registrationSuccess by viewModel.registrationSuccess.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Total steps: 3 feature pages + 4 registration pages (name, email, phone, password)
    val totalSteps = 7
    val pagerState = rememberPagerState(initialPage = 0) { totalSteps }

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onComplete()
            viewModel.resetRegistrationSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Decorative background
        OnboardingBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with back button and progress
            OnboardingTopBar(
                currentStep = pagerState.currentPage,
                totalSteps = totalSteps,
                canGoBack = pagerState.currentPage > 0,
                onBack = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            )

            // Content pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = false // Disable swipe, use buttons only
            ) { page ->
                when (page) {
                    0 -> FeatureStep(feature = onboardingFeatures[0])
                    1 -> FeatureStep(feature = onboardingFeatures[1])
                    2 -> FeatureStep(feature = onboardingFeatures[2])
                    3 -> NameStep(
                        displayName = uiState.displayName,
                        onNameChange = viewModel::onDisplayNameChange,
                        error = if (uiState.displayNameError) "Le nom est requis" else null
                    )
                    4 -> EmailStep(
                        email = uiState.email,
                        onEmailChange = viewModel::onEmailChange,
                        error = uiState.emailError
                    )
                    5 -> PhoneStep(
                        phone = uiState.phoneNumber,
                        onPhoneChange = viewModel::onPhoneNumberChange
                    )
                    6 -> PasswordStep(
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        onPasswordChange = viewModel::onPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        passwordError = uiState.passwordError,
                        confirmPasswordError = uiState.confirmPasswordError
                    )
                }
            }

            // Bottom navigation
            OnboardingBottomBar(
                currentStep = pagerState.currentPage,
                totalSteps = totalSteps,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onNext = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < totalSteps - 1) {
                            // Validate current step before proceeding
                            val canProceed = when (pagerState.currentPage) {
                                3 -> viewModel.validateName()
                                4 -> viewModel.validateEmail()
                                5 -> true // Phone is optional
                                else -> true
                            }
                            if (canProceed) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // Final step - create account
                            if (viewModel.validatePassword()) {
                                viewModel.register()
                            }
                        }
                    }
                },
                onSkip = {
                    coroutineScope.launch {
                        // Skip to registration steps (page 3)
                        pagerState.animateScrollToPage(3)
                    }
                },
                showSkip = pagerState.currentPage < 3
            )
        }
    }
}

@Composable
private fun OnboardingBackground() {
    val infiniteTransition = rememberInfiniteTransition()

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Top right orb
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-20).dp + offset.dp)
                .size(200.dp)
                .blur(80.dp)
                .background(
                    OnboardingTheme.primaryAccent.copy(alpha = 0.15f),
                    CircleShape
                )
        )

        // Bottom left orb
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp - offset.dp)
                .size(180.dp)
                .blur(70.dp)
                .background(
                    OnboardingTheme.secondaryAccent.copy(alpha = 0.12f),
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
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
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
                    .background(OnboardingTheme.surfaceInput)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = OnboardingTheme.textPrimary
                )
            }
        }

        if (!canGoBack) {
            Spacer(modifier = Modifier.width(44.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Progress indicator
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val width by animateDpAsState(
                targetValue = if (index == currentStep) 24.dp else 8.dp,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    OnboardingTheme.gradientStart,
                                    OnboardingTheme.gradientEnd
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.2f),
                                    Color.Gray.copy(alpha = 0.2f)
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
    isLoading: Boolean,
    errorMessage: String?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    showSkip: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = OnboardingTheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Main action button
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
                                OnboardingTheme.gradientStart,
                                OnboardingTheme.gradientEnd
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
                            text = when {
                                currentStep < 3 -> "Continuer"
                                currentStep == totalSteps - 1 -> "Créer mon compte"
                                else -> "Suivant"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        if (currentStep < totalSteps - 1) {
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

        // Skip button
        AnimatedVisibility(visible = showSkip) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = "Passer cette étape",
                    color = OnboardingTheme.textSecondary,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// Feature presentation step
@Composable
private fun FeatureStep(feature: OnboardingStep) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(feature) {
        isVisible = false
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                OnboardingTheme.gradientStart.copy(alpha = 0.1f),
                                OnboardingTheme.gradientEnd.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = OnboardingTheme.primaryAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = feature.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingTheme.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = feature.description,
                    fontSize = 17.sp,
                    color = OnboardingTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }
    }
}

// Registration step components
@Composable
private fun NameStep(
    displayName: String,
    onNameChange: (String) -> Unit,
    error: String?
) {
    RegistrationStepLayout(
        title = "Comment vous appelez-vous ?",
        subtitle = "Entrez votre nom pour personnaliser votre expérience"
    ) {
        OnboardingTextField(
            value = displayName,
            onValueChange = onNameChange,
            label = "Votre nom",
            leadingIcon = Icons.Default.Person,
            error = error,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    error: String?
) {
    RegistrationStepLayout(
        title = "Quelle est votre adresse email ?",
        subtitle = "Nous l'utiliserons pour sécuriser votre compte"
    ) {
        OnboardingTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Default.Email,
            error = error,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun PhoneStep(
    phone: String,
    onPhoneChange: (String) -> Unit
) {
    RegistrationStepLayout(
        title = "Votre numéro de téléphone",
        subtitle = "Optionnel - Pour une récupération de compte facilitée"
    ) {
        OnboardingTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Téléphone (optionnel)",
            leadingIcon = Icons.Default.Phone,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun PasswordStep(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    passwordError: String?,
    confirmPasswordError: String?
) {
    val focusManager = LocalFocusManager.current

    RegistrationStepLayout(
        title = "Créez votre mot de passe",
        subtitle = "Choisissez un mot de passe sécurisé"
    ) {
        OnboardingTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Mot de passe",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            error = passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirmer le mot de passe",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            error = confirmPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Password requirements
        PasswordRequirements(password = password)
    }
}

@Composable
private fun RegistrationStepLayout(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(title) {
        isVisible = false
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { -30 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingTheme.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subtitle,
                    fontSize = 15.sp,
                    color = OnboardingTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400, delayMillis = 150)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    }
}

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (error != null) OnboardingTheme.error else OnboardingTheme.primaryAccent
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                            tint = OnboardingTheme.textSecondary
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            isError = error != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OnboardingTheme.primaryAccent,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = OnboardingTheme.surfaceInput.copy(alpha = 0.5f),
                unfocusedContainerColor = OnboardingTheme.surfaceInput.copy(alpha = 0.3f),
                errorBorderColor = OnboardingTheme.error,
                cursorColor = OnboardingTheme.primaryAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        AnimatedVisibility(visible = error != null) {
            error?.let {
                Text(
                    text = it,
                    color = OnboardingTheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirements(password: String) {
    val requirements = listOf(
        "Au moins 8 caractères" to (password.length >= 8),
        "Une majuscule" to password.any { it.isUpperCase() },
        "Une minuscule" to password.any { it.isLowerCase() },
        "Un chiffre" to password.any { it.isDigit() }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OnboardingTheme.surfaceInput.copy(alpha = 0.5f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        requirements.forEach { (text, isValid) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isValid) OnboardingTheme.success else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = if (isValid) OnboardingTheme.success else OnboardingTheme.textSecondary
                )
            }
        }
    }
}