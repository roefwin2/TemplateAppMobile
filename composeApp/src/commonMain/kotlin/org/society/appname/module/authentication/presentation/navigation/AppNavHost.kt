package org.society.appname.module.authentication.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import org.society.appname.module.authentication.presentation.MainViewModel
import org.society.appname.module.authentication.presentation.SessionState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val sessionState by mainViewModel.sessionState.collectAsState()
    val navController = rememberNavController()

    when (sessionState) {
        is SessionState.Loading -> {
            // Écran de chargement
        }
        is SessionState.Unauthenticated -> {
            AuthNavHost(
                navController = navController,
                onLoginSuccess = { /* La session sera automatiquement mise à jour */ }
            )
        }
        is SessionState.Authenticated -> {
            MainNavHost(
                navController = navController,
                onLogout = { /* La déconnexion est gérée par le ViewModel */ }
            )
        }
    }
}