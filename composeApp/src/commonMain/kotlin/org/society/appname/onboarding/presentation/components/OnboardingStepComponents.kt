package org.society.appname.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.society.appname.onboarding.domain.model.ChoiceOption
import org.society.appname.onboarding.domain.model.OnboardingStepConfig

// ===== Theme Colors =====
object OnboardingColors {
    val Primary = Color(0xFF6366F1)
    val PrimaryLight = Color(0xFF8B5CF6)
    val Background = Color(0xFFFAFAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSelected = Color(0xFFF0F0FF)
    val TextPrimary = Color(0xFF1F2937)
    val TextSecondary = Color(0xFF6B7280)
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)
    val Border = Color(0xFFE5E7EB)
    val BorderSelected = Color(0xFF6366F1)
}

// ===== INTRO STEP =====
@Composable
fun IntroStepContent(
    config: OnboardingStepConfig.Intro,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        isVisible = false
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.8f)
        ) {
            config.emoji?.let { emoji ->
                Text(
                    text = emoji,
                    fontSize = 80.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                tween(
                    500,
                    delayMillis = 200
                )
            ) + slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = config.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = config.description,
                    fontSize = 17.sp,
                    color = OnboardingColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }
    }
}

// ===== SINGLE CHOICE STEP =====
@Composable
fun SingleChoiceStepContent(
    config: OnboardingStepConfig.SingleChoice,
    selectedOptionId: String?,
    onOptionSelected: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    StepLayout(
        question = config.question,
        description = config.description,
        error = error,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            config.options.forEach { option ->
                ChoiceOptionItem(
                    option = option,
                    isSelected = option.id == selectedOptionId,
                    onClick = { onOptionSelected(option.id) }
                )
            }
        }
    }
}

// ===== MULTI CHOICE STEP =====
@Composable
fun MultiChoiceStepContent(
    config: OnboardingStepConfig.MultiChoice,
    selectedOptionIds: List<String>,
    onOptionToggled: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    StepLayout(
        question = config.question,
        description = config.description,
        error = error,
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(config.options) { option ->
                ChoiceChip(
                    option = option,
                    isSelected = option.id in selectedOptionIds,
                    onClick = { onOptionToggled(option.id) }
                )
            }
        }

        config.maxSelections?.let { max ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selectedOptionIds.size}/$max sélectionnés",
                fontSize = 13.sp,
                color = OnboardingColors.TextSecondary
            )
        }
    }
}

// ===== MULTI CHOICE GROUPED STEP =====
@Composable
fun MultiChoiceGroupedStepContent(
    config: OnboardingStepConfig.MultiChoiceGrouped,
    selectedOptionIds: List<String>,
    onOptionToggled: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    StepLayout(
        question = config.question,
        description = config.description,
        error = error,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.heightIn(max = 450.dp)
        ) {
            items(config.groups) { group ->
                Column {
                    Text(
                        text = group.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnboardingColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        group.options.forEach { option ->
                            SmallChoiceChip(
                                option = option,
                                isSelected = option.id in selectedOptionIds,
                                onClick = { onOptionToggled(option.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===== TEXT INPUT STEP =====
@Composable
fun TextInputStepContent(
    config: OnboardingStepConfig.TextInput,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    StepLayout(
        question = config.question,
        description = config.description,
        error = error,
        modifier = modifier
    ) {
        OnboardingTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = config.placeholder,
            maxLength = config.maxLength
        )
    }
}

@Composable
fun TextInputOptionalStepContent(
    config: OnboardingStepConfig.TextInputOptional,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    StepLayout(
        question = config.question,
        description = config.description,
        error = null,
        modifier = modifier
    ) {
        OnboardingTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = config.placeholder,
            maxLength = config.maxLength
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Vous pouvez passer cette étape",
            fontSize = 13.sp,
            color = OnboardingColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ===== REGISTRATION STEP =====
@Composable
fun RegistrationStepContent(
    config: OnboardingStepConfig.Registration,
    displayName: String,
    email: String,
    phoneNumber: String,
    password: String,
    confirmPassword: String,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    displayNameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    StepLayout(
        question = config.title,
        description = config.description,
        error = null,
        modifier = modifier
    ) {
        // Name
        OnboardingTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            placeholder = "Votre nom",
            leadingIcon = Icons.Default.Person,
            error = displayNameError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OnboardingTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Adresse email",
            leadingIcon = Icons.Default.Email,
            error = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone (optional)
        OnboardingTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = "Téléphone (optionnel)",
            leadingIcon = Icons.Default.Phone,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OnboardingTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Mot de passe",
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

        // Confirm password
        OnboardingTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Confirmer le mot de passe",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            error = confirmPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password requirements
        PasswordRequirements(password = password)
    }
}

// ===== SUMMARY STEP =====
@Composable
fun SummaryStepContent(
    config: OnboardingStepConfig.Summary,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        isVisible = false
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.5f)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                OnboardingColors.Success.copy(alpha = 0.2f),
                                OnboardingColors.Success.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = OnboardingColors.Success
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                tween(
                    500,
                    delayMillis = 300
                )
            ) + slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = config.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = config.description,
                    fontSize = 16.sp,
                    color = OnboardingColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ===== REUSABLE COMPONENTS =====

@Composable
private fun StepLayout(
    question: String,
    description: String?,
    error: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(question) {
        isVisible = false
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -20 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = question,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                description?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        fontSize = 15.sp,
                        color = OnboardingColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                tween(
                    300,
                    delayMillis = 150
                )
            ) + slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }

        // Error message
        AnimatedVisibility(visible = error != null) {
            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = OnboardingColors.Error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ChoiceOptionItem(
    option: ChoiceOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) OnboardingColors.SurfaceSelected
                else OnboardingColors.Surface
            )
            .border(
                width = 2.dp,
                color = if (isSelected) OnboardingColors.BorderSelected else OnboardingColors.Border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        option.emoji?.let { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        Text(
            text = option.label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = OnboardingColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(OnboardingColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    option: ChoiceOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) OnboardingColors.SurfaceSelected
                else OnboardingColors.Surface
            )
            .border(
                width = 2.dp,
                color = if (isSelected) OnboardingColors.BorderSelected else OnboardingColors.Border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        option.emoji?.let { emoji ->
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = option.label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = OnboardingColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(OnboardingColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallChoiceChip(
    option: ChoiceOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) OnboardingColors.Primary.copy(alpha = 0.15f)
                else OnboardingColors.Surface
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) OnboardingColors.Primary else OnboardingColors.Border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = OnboardingColors.Primary,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp)
            )
        }

        Text(
            text = option.label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) OnboardingColors.Primary else OnboardingColors.TextPrimary
        )
    }
}

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isPassword: Boolean = false,
    error: String? = null,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = OnboardingColors.TextSecondary.copy(alpha = 0.6f)
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (error != null) OnboardingColors.Error else OnboardingColors.Primary
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = OnboardingColors.TextSecondary
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
                focusedBorderColor = OnboardingColors.Primary,
                unfocusedBorderColor = OnboardingColors.Border,
                errorBorderColor = OnboardingColors.Error,
                focusedContainerColor = OnboardingColors.Surface,
                unfocusedContainerColor = OnboardingColors.Surface,
                cursorColor = OnboardingColors.Primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        AnimatedVisibility(visible = error != null) {
            error?.let {
                Text(
                    text = it,
                    color = OnboardingColors.Error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        maxLength?.let {
            Text(
                text = "${value.length}/$it",
                fontSize = 12.sp,
                color = OnboardingColors.TextSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp, end = 8.dp)
            )
        }
    }
}

@Composable
private fun PasswordRequirements(password: String) {
    val requirements = listOf(
        "8 caractères minimum" to (password.length >= 8),
        "Une majuscule" to password.any { it.isUpperCase() },
        "Une minuscule" to password.any { it.isLowerCase() },
        "Un chiffre" to password.any { it.isDigit() }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OnboardingColors.Surface)
            .border(1.dp, OnboardingColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        requirements.forEach { (text, isValid) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isValid) Icons.Default.CheckCircle
                    else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isValid) OnboardingColors.Success
                    else OnboardingColors.TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = text,
                    fontSize = 13.sp,
                    color = if (isValid) OnboardingColors.Success
                    else OnboardingColors.TextSecondary
                )
            }
        }
    }
}

// Flow Row pour les chips groupés
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Note: Dans une vraie app, utilisez FlowRow de Compose Foundation
    // Pour simplifier, on utilise une Column avec des Rows
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        Row(
            horizontalArrangement = horizontalArrangement,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
private fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()
