package org.society.appname.authentication.providers

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun rememberProviderSignInLauncher(
    onResult: (Result<ProviderSignInResult>) -> Unit
): ProviderSignInLauncher {
    val authManager: SocialAuthManager = koinInject()
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        scope.launch {
            val authResult = authManager.handleGoogleSignInResult(result.data)
            onResult(authResult)
        }
    }

    return remember(authManager, launcher) {
        object : ProviderSignInLauncher {
            override fun launch() {
                val intent = authManager.prepareGoogleSignIn() as? Intent
                if (intent != null) {
                    launcher.launch(intent)
                } else {
                    onResult(Result.failure(Exception("Google Sign-In non initialisé")))
                }
            }
        }
    }
}