package org.society.appname.payment.presentation

import androidx.compose.runtime.Composable
import org.society.appname.payment.domain.PaymentResult

/**
 * Configuration pour PaymentSheet
 */
data class PaymentSheetConfig(
    val publishableKey: String,
    val merchantName: String = "Paiement",
    val customerId: String? = null,
    val ephemeralKeySecret: String? = null,
    val allowsDelayedPaymentMethods: Boolean = false
)

/**
 * Interface commune pour PaymentSheet
 * Implémentée différemment sur Android (Stripe SDK) et iOS (Stripe SDK iOS)
 */
expect class PaymentSheetLauncher {

    /**
     * Indique si le launcher est prêt à être utilisé
     */
    val isReady: Boolean

    /**
     * Présente la bottom sheet de paiement
     *
     * @param clientSecret Le client_secret du PaymentIntent
     * @param onResult Callback avec le résultat du paiement
     */
    fun present(
        clientSecret: String,
        onResult: (PaymentResult) -> Unit
    )
}

/**
 * Crée un PaymentSheetLauncher
 * Doit être appelé dans un contexte Composable sur Android
 */
@Composable
expect fun rememberPaymentSheetLauncher(
    config: PaymentSheetConfig
): PaymentSheetLauncher