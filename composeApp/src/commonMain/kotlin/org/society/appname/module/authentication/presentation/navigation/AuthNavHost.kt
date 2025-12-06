package org.society.appname.module.authentication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.society.appname.module.authentication.presentation.login.LoginScreen
import org.society.appname.module.authentication.presentation.register.RegisterScreen

@Composable
fun AuthNavHost(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = onLoginSuccess
            )
        }
    }
}