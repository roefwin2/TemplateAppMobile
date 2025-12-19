package org.society.appname.authentication.presentation.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.koin.compose.viewmodel.koinViewModel
import org.society.appname.authentication.providers.rememberProviderSignInLauncher

// Login Theme (coherent with other screens)
object LoginTheme {
    val primaryDark = Color(0xFF1A1A2E)
    val primaryAccent = Color(0xFF6366F1)
    val secondaryAccent = Color(0xFF8B5CF6)
    val surfaceInput = Color(0xFFF1F5F9)
    val textPrimary = Color(0xFF1F2937)
    val textSecondary = Color(0xFF6B7280)
    val gradientStart = Color(0xFF6366F1)
    val gradientEnd = Color(0xFFA855F7)
    val error = Color(0xFFEF4444)
    val divider = Color(0xFFE5E7EB)
}

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val focusManager = LocalFocusManager.current

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
            viewModel.resetLoginSuccess()
        }
    }

    val providerSignLauncher = rememberProviderSignInLauncher { result ->
        result.fold(
            onSuccess = { auth ->
                viewModel.onLoginClicked(auth)
            },
            onFailure = { error ->
                println("❌ Erreur: ${error.message}")
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Decorative background
        LoginBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar with back button
            LoginTopBar(onBack = onNavigateBack)

            Spacer(modifier = Modifier.height(24.dp))

            // Header
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Bon retour ! 👋",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoginTheme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Connectez-vous pour continuer votre aventure",
                        fontSize = 16.sp,
                        color = LoginTheme.textSecondary,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Form
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Email field
                    LoginTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    LoginTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Mot de passe",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.onLoginClicked()
                            }
                        )
                    )

                    // Forgot password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onForgotPassword) {
                            Text(
                                text = "Mot de passe oublié ?",
                                color = LoginTheme.primaryAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Error message
                    AnimatedVisibility(visible = uiState.errorMessage != null) {
                        uiState.errorMessage?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LoginTheme.error.copy(alpha = 0.1f))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LoginTheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = it,
                                    color = LoginTheme.error,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login button
                    Button(
                        onClick = { viewModel.onLoginClicked() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            LoginTheme.gradientStart,
                                            LoginTheme.gradientEnd
                                        )
                                    ),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Se connecter",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = LoginTheme.divider
                        )
                        Text(
                            text = "ou continuer avec",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = LoginTheme.textSecondary,
                            fontSize = 14.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = LoginTheme.divider
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Social login buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SocialLoginButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AccountCircle, // Replace with Google icon
                            label = "Google",
                            onClick = { providerSignLauncher.launch() }
                        )

                        SocialLoginButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Phone, // Replace with Apple icon
                            label = "Apple",
                            onClick = { providerSignLauncher.launch() }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun LoginBackground() {
    val infiniteTransition = rememberInfiniteTransition()

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Top right orb
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp + offset.dp)
                .size(220.dp)
                .blur(90.dp)
                .background(
                    LoginTheme.primaryAccent.copy(alpha = 0.12f),
                    CircleShape
                )
        )

        // Center left orb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-60).dp, y = 50.dp - offset.dp)
                .size(180.dp)
                .blur(70.dp)
                .background(
                    LoginTheme.secondaryAccent.copy(alpha = 0.1f),
                    CircleShape
                )
        )
    }
}

@Composable
private fun LoginTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LoginTheme.surfaceInput)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = LoginTheme.textPrimary
            )
        }
    }
}

@Composable
private fun LoginTextField(
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
                    tint = if (error != null) LoginTheme.error else LoginTheme.primaryAccent
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                            tint = LoginTheme.textSecondary
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
                focusedBorderColor = LoginTheme.primaryAccent,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = LoginTheme.surfaceInput.copy(alpha = 0.5f),
                unfocusedContainerColor = LoginTheme.surfaceInput.copy(alpha = 0.3f),
                errorBorderColor = LoginTheme.error,
                cursorColor = LoginTheme.primaryAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        AnimatedVisibility(visible = error != null) {
            error?.let {
                Text(
                    text = it,
                    color = LoginTheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Gray.copy(alpha = 0.3f),
                    Color.Gray.copy(alpha = 0.3f)
                )
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LoginTheme.textPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = LoginTheme.textPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}