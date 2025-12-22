package org.society.appname.authentication.presentation.password

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Modern Forgot Password Screen
 *
 * Design cohérent avec WelcomeScreen, OnboardingScreen et LoginScreen
 */
object ForgotPasswordScreenTestTags {
    const val EmailField = "forgot_password_email_field"
    const val SendButton = "forgot_password_send_button"
    const val SuccessMessage = "forgot_password_success_message"
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsState().value
    val scrollState = rememberScrollState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }

    // Animation state
    var isVisible by remember { mutableStateOf(false) }

    // Auto-focus sur le champ email au démarrage
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        delay(200)
        emailFocusRequester.requestFocus()
    }

    // Fermer l'écran après succès
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            delay(3000)
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onNavigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
            }

            // Content
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(300)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6C63FF).copy(alpha = 0.2f),
                                        Color(0xFF9D4EDD).copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockReset,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF9D4EDD)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Mot de passe oublié ?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Entrez votre email et nous vous enverrons\nun lien pour réinitialiser votre mot de passe",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Email Field
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        placeholder = {
                            Text(
                                text = "Adresse email",
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .testTag(ForgotPasswordScreenTestTags.EmailField),
                        singleLine = true,
                        enabled = !state.isSuccess,
                        isError = state.errorMessage != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (state.email.isNotBlank() && !state.isLoading) {
                                    viewModel.onSendResetLinkClicked()
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color(0xFF6C63FF),
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            errorBorderColor = Color(0xFFE94560),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Message
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE94560).copy(alpha = 0.1f))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFE94560),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = state.errorMessage ?: "",
                                    color = Color(0xFFE94560),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Success Message
                    AnimatedVisibility(
                        visible = state.isSuccess,
                        enter = fadeIn() + expandVertically() + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF00C853).copy(alpha = 0.15f),
                                            Color(0xFF69F0AE).copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                                .testTag(ForgotPasswordScreenTestTags.SuccessMessage)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00C853),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Email envoyé !",
                                    color = Color(0xFF00C853),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Vérifiez votre boîte de réception pour\nle lien de réinitialisation",
                                    color = Color(0xFF69F0AE),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    if (!state.isSuccess) {
                        Spacer(modifier = Modifier.height(32.dp))

                        // Send Reset Link Button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                viewModel.onSendResetLinkClicked()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag(ForgotPasswordScreenTestTags.SendButton),
                            enabled = state.email.isNotBlank() && !state.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C63FF),
                                disabledContainerColor = Color(0xFF6C63FF).copy(alpha = 0.4f)
                            )
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Envoyer le lien",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Back to Login
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF6C63FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retour à la connexion",
                            color = Color(0xFF6C63FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
