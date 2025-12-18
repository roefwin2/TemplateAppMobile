package org.society.appname.authentication.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.society.appname.authentication.presentation.login.LoginScreen
import org.society.appname.authentication.presentation.welcome.WelcomeScreen
import org.society.appname.onboarding.presentation.OnboardingScreen

/**
 * Authentication flow navigation routes
 */
object AuthRoutes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val FORGOT_PASSWORD = "forgot_password"
}

/**
 * Main authentication navigation host
 *
 * Flow:
 * 1. Welcome Screen → "Get Started" → Onboarding (with registration at the end)
 *                   → "Login" → Login Screen
 * 2. Login Success → onAuthComplete()
 * 3. Onboarding Complete (account created) → onAuthComplete()
 */
@Composable
fun AuthNavHost(
    navController: NavHostController = rememberNavController(),
    onAuthComplete: () -> Unit,
    startDestination: String = AuthRoutes.WELCOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        // Welcome Screen
        composable(
            route = AuthRoutes.WELCOME,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(AuthRoutes.ONBOARDING) {
                        launchSingleTop = true
                    }
                },
                onLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Login Screen
        composable(route = AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = onAuthComplete,
                onForgotPassword = {
                    navController.navigate(AuthRoutes.FORGOT_PASSWORD) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Onboarding Screen (with registration)
        composable(route = AuthRoutes.ONBOARDING) {
            OnboardingScreen(
                onComplete = onAuthComplete
            )
        }

        // Forgot Password Screen (placeholder)
        composable(route = AuthRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Simple forgot password placeholder screen
 * You can expand this with full functionality
 */
@Composable
private fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    // Placeholder - implement as needed
    // This would contain email input and password reset logic
}

/**
 * Navigation extension for checking if user should see onboarding
 * Use this to determine the start destination
 */
fun shouldShowOnboarding(
    isFirstLaunch: Boolean,
    isLoggedIn: Boolean
): String {
    return when {
        isLoggedIn -> AuthRoutes.WELCOME // Or skip directly to main app
        isFirstLaunch -> AuthRoutes.WELCOME
        else -> AuthRoutes.LOGIN // Returning user who logged out
    }
}