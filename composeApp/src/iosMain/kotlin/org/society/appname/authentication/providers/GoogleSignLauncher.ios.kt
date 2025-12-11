package org.society.appname.authentication.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.society.appname.authentication.providers.ProviderSignInResult.AppleAuthResult
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

// Classe pour retenir les delegates en mémoire
private class AppleSignInDelegates(
    val controllerDelegate: ASAuthorizationControllerDelegateProtocol,
    val presentationProvider: ASAuthorizationControllerPresentationContextProvidingProtocol
)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberProviderSignInLauncher(
    onResult: (Result<ProviderSignInResult>) -> Unit
): ProviderSignInLauncher {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        object : ProviderSignInLauncher {
            // Variable pour retenir les delegates
            private var currentDelegates: AppleSignInDelegates? = null

            override fun launch() {
                println("🍎 Lancement Apple Sign-In...")
                println("📱 ViewController: $viewController")
                println("🪟 Window: ${viewController.view.window}")

                if (viewController.view.window == null) {
                    println("❌ ERREUR: Window est null!")
                    onResult(Result.failure(Exception("Window is null. Cannot present Apple Sign-In.")))
                    return
                }

                MainScope().launch {
                    try {
                        val result = performAppleSignIn(viewController) { delegates ->
                            // Retenir les delegates en mémoire
                            currentDelegates = delegates
                        }
                        println("✅ Résultat reçu, appel onResult")
                        currentDelegates = null // Libérer après utilisation
                        onResult(result)
                    } catch (e: Exception) {
                        println("❌ Exception capturée: ${e.message}")
                        e.printStackTrace()
                        currentDelegates = null
                        onResult(Result.failure(e))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun performAppleSignIn(
    viewController: UIViewController,
    retainDelegates: (AppleSignInDelegates) -> Unit
): Result<ProviderSignInResult> = suspendCancellableCoroutine { continuation ->

    println("🔧 Configuration Apple Sign-In...")

    try {
        val provider = ASAuthorizationAppleIDProvider()
        println("✅ Provider créé")

        val request = provider.createRequest()
        println("✅ Request créée")

        // 🎯 IMPORTANT: Demander les scopes fullName et email
        request.requestedScopes = listOf(
            ASAuthorizationScopeFullName,
            ASAuthorizationScopeEmail
        )
        println("✅ Scopes configurés")

        val controller = ASAuthorizationController(
            authorizationRequests = listOf(request)
        )
        println("✅ Controller créé")

        // Delegate pour gérer les callbacks
        val delegate = object : NSObject(),
            ASAuthorizationControllerDelegateProtocol {

            override fun authorizationController(
                controller: ASAuthorizationController,
                didCompleteWithAuthorization: ASAuthorization
            ) {
                println("✅ ✅ ✅ SUCCESS DELEGATE APPELÉ ✅ ✅ ✅")

                try {
                    val credential = didCompleteWithAuthorization.credential
                            as? ASAuthorizationAppleIDCredential

                    if (credential == null) {
                        println("❌ Credential null")
                        continuation.resume(Result.failure(Exception("Apple ID credential is null")))
                        return
                    }

                    println("✅ Credential obtenu")

                    val tokenData = credential.identityToken
                    val token = tokenData?.let { data ->
                        NSString.create(
                            data = data,
                            encoding = NSUTF8StringEncoding
                        ) as? String
                    }

                    if (token == null) {
                        println("❌ Token null")
                        continuation.resume(Result.failure(Exception("Failed to get identity token")))
                        return
                    }

                    val authCodeData = credential.authorizationCode
                    val authCode = authCodeData?.let { data ->
                        NSString.create(
                            data = data,
                            encoding = NSUTF8StringEncoding
                        ) as? String
                    }

                    // 🎯 RÉCUPÉRER LE NOM COMPLET
                    val fullNameComponents = credential.fullName
                    val fullName = if (fullNameComponents != null) {
                        val givenName = fullNameComponents.givenName ?: ""
                        val familyName = fullNameComponents.familyName ?: ""

                        when {
                            givenName.isNotBlank() && familyName.isNotBlank() ->
                                "$givenName $familyName".trim()

                            givenName.isNotBlank() -> givenName.trim()
                            familyName.isNotBlank() -> familyName.trim()
                            else -> null
                        }
                    } else {
                        null
                    }

                    // Récupérer l'email
                    val email = credential.email

                    println("✅ Token récupéré: ${token.take(20)}...")
                    println("✅ AuthCode: ${authCode?.take(20) ?: "null"}")
                    println("✅ Email: ${email ?: "null"}")
                    println("✅ UserID: ${credential.user}")
                    println("🎯 FullName récupéré: ${fullName ?: "null"}")

                    if (fullName == null) {
                        println("⚠️ fullName est null - probablement pas la première connexion")
                        println("⚠️ Pour tester: allez sur appleid.apple.com et supprimez l'app")
                    }

                    continuation.resume(
                        Result.success(
                            AppleAuthResult(
                                idToken = token,
                                rawNonce = authCode ?: "",
                                fullName = fullName
                            )
                        )
                    )
                    println("✅ Resume appelé avec succès")
                } catch (e: Exception) {
                    println("❌ Exception dans success delegate: ${e.message}")
                    e.printStackTrace()
                    continuation.resume(Result.failure(e))
                }
            }

            override fun authorizationController(
                controller: ASAuthorizationController,
                didCompleteWithError: NSError
            ) {
                val errorCode = didCompleteWithError.code
                val errorMsg = didCompleteWithError.localizedDescription

                println("❌ ❌ ❌ ERROR DELEGATE APPELÉ ❌ ❌ ❌")
                println("❌ Error: $errorMsg (code: $errorCode)")
                println("❌ Domain: ${didCompleteWithError.domain}")

                if (errorCode == 1001L) {
                    println("❌ Utilisateur a annulé")
                    continuation.resume(Result.failure(Exception("Connexion annulée par l'utilisateur")))
                } else {
                    continuation.resume(Result.failure(Exception("Apple Sign-In error: $errorMsg")))
                }
            }
        }
        println("✅ Delegate créé")

        // Presentation provider
        val presentationProvider = object : NSObject(),
            ASAuthorizationControllerPresentationContextProvidingProtocol {

            override fun presentationAnchorForAuthorizationController(
                controller: ASAuthorizationController
            ): ASPresentationAnchor {
                println("🪟 🪟 🪟 PRESENTATION ANCHOR APPELÉ 🪟 🪟 🪟")
                val window = viewController.view.window
                println("🪟 Window obtenue: $window")
                return window ?: throw IllegalStateException("Window is null in presentationAnchor")
            }
        }
        println("✅ Presentation provider créé")

        // IMPORTANT: Retenir les delegates en mémoire
        val delegates = AppleSignInDelegates(delegate, presentationProvider)
        retainDelegates(delegates)
        println("✅ Delegates retenus en mémoire")

        // Assigner les delegates au controller
        controller.delegate = delegate
        controller.presentationContextProvider = presentationProvider
        println("✅ Delegates assignés au controller")

        println("🚀 Appel de performRequests()")
        controller.performRequests()
        println("✅ performRequests() appelé - En attente du callback...")

        // Gestion de l'annulation
        continuation.invokeOnCancellation {
            println("🚫 Sign-In annulé par cancellation")
        }

    } catch (e: Exception) {
        println("❌ Exception lors de la configuration: ${e.message}")
        e.printStackTrace()
        continuation.resume(Result.failure(e))
    }
}