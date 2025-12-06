package org.society.appname.module.authentication.presentation.mainscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.society.appname.module.authentication.presentation.MainViewModel
import org.society.appname.module.authentication.presentation.SessionState
import org.society.appname.module.authentication.presentation.navigation.BottomNavItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    when (val state = sessionState) {
        is SessionState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SessionState.Authenticated -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                when (selectedTab) {
                                    0 -> "Accueil"
                                    1 -> "Profil"
                                    2 -> "Paramètres"
                                    else -> "App"
                                }
                            ) 
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    viewModel.logout()
                                    onLogout()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Se déconnecter"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
                            label = { Text("Accueil") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                            label = { Text("Profil") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Paramètres") },
                            label = { Text("Paramètres") },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(user = state.user)
                        1 -> ProfileScreen(user = state.user)
                        2 -> SettingsScreen(user = state.user)
                    }
                }
            }
        }
        is SessionState.Unauthenticated -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Session expirée")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLogout) {
                        Text("Retour à la connexion")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(user: org.society.appname.module.authentication.User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bienvenue !",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user.displayName ?: user.email ?: "Utilisateur",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Informations",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Email: ${user.email ?: "Non renseigné"}")
                user.number?.let {
                    Text("Téléphone: $it")
                }
                Text("Email vérifié: ${if (user.isEmailVerified) "Oui" else "Non"}")
            }
        }
    }
}

@Composable
private fun ProfileScreen(user: org.society.appname.module.authentication.User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Avatar / Icône de profil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Informations utilisateur
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Mon Profil",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.Person,
                    label = "Nom",
                    value = user.displayName ?: "Non renseigné"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user.email ?: "Non renseigné"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                user.number?.let { number ->
                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Téléphone",
                        value = number
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                ProfileInfoRow(
                    icon = Icons.Default.VerifiedUser,
                    label = "Email vérifié",
                    value = if (user.isEmailVerified) "Oui" else "Non"
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SettingsScreen(user: org.society.appname.module.authentication.User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Paramètres",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Gérer les notifications"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingItem(
                    icon = Icons.Default.Security,
                    title = "Sécurité",
                    subtitle = "Mot de passe et authentification"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingItem(
                    icon = Icons.Default.Language,
                    title = "Langue",
                    subtitle = "Français"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingItem(
                    icon = Icons.Default.DarkMode,
                    title = "Thème",
                    subtitle = "Clair / Sombre"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "À propos",
                    subtitle = "Version 1.0.0"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingItem(
                    icon = Icons.Default.Help,
                    title = "Aide",
                    subtitle = "Centre d'aide et FAQ"
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}