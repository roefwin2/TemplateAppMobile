package org.society.appname.authentication.providers

import androidx.compose.runtime.Composable

@Composable
expect fun rememberProviderSignInLauncher(
    onResult: (Result<ProviderSignInResult>) -> Unit
): ProviderSignInLauncher

/**
 * Interface pour lancer le Google Sign-In
 */
interface ProviderSignInLauncher {
    fun launch()
}