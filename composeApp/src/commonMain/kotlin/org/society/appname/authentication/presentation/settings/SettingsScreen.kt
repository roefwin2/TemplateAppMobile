package org.society.appname.authentication.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.society.appname.authentication.User

/**
 * Modern Settings Screen
 *
 * Design cohérent avec le flux d'authentification
 */
@Composable
fun SettingsScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Animation state
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Handle logout success
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    // Handle account deleted
    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onAccountDeleted()
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            SettingsTopBar(onNavigateBack = onNavigateBack)

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
                        .padding(horizontal = 20.dp)
                        .navigationBarsPadding()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // User Profile Card
                    UserProfileCard(user = user)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Settings Sections
                    SettingsSection(title = "Préférences") {
                        SettingItem(
                            icon = Icons.Rounded.Notifications,
                            title = "Notifications",
                            subtitle = "Gérer les notifications",
                            onClick = { /* TODO */ }
                        )
                        SettingItem(
                            icon = Icons.Rounded.DarkMode,
                            title = "Thème",
                            subtitle = "Clair / Sombre",
                            onClick = { /* TODO */ }
                        )
                        SettingItem(
                            icon = Icons.Rounded.Language,
                            title = "Langue",
                            subtitle = "Français",
                            onClick = { /* TODO */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSection(title = "Sécurité") {
                        SettingItem(
                            icon = Icons.Rounded.Lock,
                            title = "Mot de passe",
                            subtitle = "Modifier votre mot de passe",
                            onClick = { /* TODO */ }
                        )
                        SettingItem(
                            icon = Icons.Rounded.Security,
                            title = "Authentification",
                            subtitle = "Options de sécurité",
                            onClick = { /* TODO */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSection(title = "Informations") {
                        SettingItem(
                            icon = Icons.Rounded.Info,
                            title = "À propos",
                            subtitle = "Version 1.0.0",
                            onClick = { /* TODO */ }
                        )
                        SettingItem(
                            icon = Icons.AutoMirrored.Rounded.Help,
                            title = "Aide",
                            subtitle = "Centre d'aide et FAQ",
                            onClick = { /* TODO */ }
                        )
                        SettingItem(
                            icon = Icons.Rounded.Description,
                            title = "Conditions d'utilisation",
                            subtitle = "Lire les CGU",
                            onClick = { /* TODO */ }
                        )
                        SettingItem(
                            icon = Icons.Rounded.PrivacyTip,
                            title = "Confidentialité",
                            subtitle = "Politique de confidentialité",
                            onClick = { /* TODO */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout Button
                    ActionButton(
                        text = "Se déconnecter",
                        icon = Icons.AutoMirrored.Rounded.ExitToApp,
                        containerColor = Color(0xFF6C63FF).copy(alpha = 0.15f),
                        contentColor = Color(0xFF6C63FF),
                        onClick = { viewModel.showLogoutDialog() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Delete Account Button
                    ActionButton(
                        text = "Supprimer mon compte",
                        icon = Icons.Rounded.DeleteForever,
                        containerColor = Color(0xFFE94560).copy(alpha = 0.15f),
                        contentColor = Color(0xFFE94560),
                        onClick = { viewModel.showDeleteDialog() }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Logout Confirmation Dialog
        if (uiState.isLogoutDialogVisible) {
            ConfirmationDialog(
                title = "Se déconnecter",
                message = "Êtes-vous sûr de vouloir vous déconnecter ?",
                confirmText = "Déconnexion",
                confirmColor = Color(0xFF6C63FF),
                isLoading = uiState.isLoggingOut,
                error = uiState.logoutError,
                onConfirm = { viewModel.logout() },
                onDismiss = { viewModel.hideLogoutDialog() }
            )
        }

        // Delete Account Confirmation Dialog
        if (uiState.isDeleteDialogVisible) {
            ConfirmationDialog(
                title = "Supprimer le compte",
                message = "Cette action est irréversible. Toutes vos données seront définitivement supprimées.",
                confirmText = "Supprimer",
                confirmColor = Color(0xFFE94560),
                isLoading = uiState.isDeleting,
                error = uiState.deleteError,
                onConfirm = { viewModel.deleteAccount() },
                onDismiss = { viewModel.hideDeleteDialog() }
            )
        }
    }
}

@Composable
private fun SettingsTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Paramètres",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun UserProfileCard(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6C63FF).copy(alpha = 0.2f),
                        Color(0xFF9D4EDD).copy(alpha = 0.15f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6C63FF),
                                Color(0xFF9D4EDD)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName?.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName ?: "Utilisateur",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email ?: "",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                if (user.isEmailVerified == true) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF00C853)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Email vérifié",
                            fontSize = 12.sp,
                            color = Color(0xFF00C853)
                        )
                    }
                }
            }

            IconButton(onClick = { /* Edit profile */ }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Modifier",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF6C63FF).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF9D4EDD)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    isLoading: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = Color(0xFF1e1e2e),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // Error message
                AnimatedVisibility(visible = error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE94560).copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                tint = Color(0xFFE94560),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error ?: "",
                                color = Color(0xFFE94560),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    disabledContainerColor = confirmColor.copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = confirmText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    text = "Annuler",
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}