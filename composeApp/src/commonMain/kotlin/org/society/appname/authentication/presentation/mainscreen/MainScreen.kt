package org.society.appname.authentication.presentation.mainscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.society.appname.authentication.User
import org.society.appname.authentication.presentation.MainViewModel
import org.society.appname.authentication.presentation.SessionState
import org.society.appname.authentication.presentation.settings.SettingsScreen
import org.society.appname.geolocation.presentation.MapScreen

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
                            icon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Paramètres"
                                )
                            },
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
                        0 -> MapScreen()
                        1 -> ProfileScreen(user = state.user)
                        2 -> SettingsScreen(
                            user = state.user,
                            onNavigateBack = { onLogout.invoke() },
                            onLogoutSuccess = { onLogout.invoke() },
                            onAccountDeleted = { onLogout.invoke() }
                        )
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

        is SessionState.NeedsOnboarding -> {

        }
    }
}

@Composable
private fun HomeScreen(user: User) {
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
private fun ProfileScreen(user: User) {
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
    icon: ImageVector,
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