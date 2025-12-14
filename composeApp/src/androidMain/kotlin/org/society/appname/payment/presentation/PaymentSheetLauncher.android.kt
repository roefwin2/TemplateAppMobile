package org.society.appname.payment.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.society.appname.payment.domain.PaymentResult

/**
 * Implémentation Android de PaymentSheetLauncher
 */
actual class PaymentSheetLauncher(
    private val activity: ComponentActivity,
    private val config: PaymentSheetConfig
) {
    private var paymentSheet: PaymentSheet? = null
    private var resultCallback: ((PaymentResult) -> Unit)? = null

    actual val isReady: Boolean
        get() = paymentSheet != null

    init {
        // Initialize Stripe
        PaymentConfiguration.init(activity, config.publishableKey)

        // Create PaymentSheet
        paymentSheet = PaymentSheet(activity) { result ->
            handleResult(result)
        }
    }

    actual fun present(
        clientSecret: String,
        onResult: (PaymentResult) -> Unit
    ) {
        resultCallback = onResult

        val configBuilder = PaymentSheet.Configuration.Builder(config.merchantName)
            .allowsDelayedPaymentMethods(config.allowsDelayedPaymentMethods)

        // Customer configuration for saved cards
        if (config.customerId != null && config.ephemeralKeySecret != null) {
            configBuilder.customer(
                PaymentSheet.CustomerConfiguration(
                    id = config.customerId,
                    ephemeralKeySecret = config.ephemeralKeySecret
                )
            )
        }

        paymentSheet?.presentWithPaymentIntent(
            paymentIntentClientSecret = clientSecret,
            configuration = configBuilder.build()
        )
    }

    private fun handleResult(result: PaymentSheetResult) {
        val paymentResult = when (result) {
            is PaymentSheetResult.Completed -> {
                PaymentResult.Success(
                    message = "Paiement réussi !",
                    paymentIntentId = null,
                    amount = null,
                    currency = null
                )
            }
            is PaymentSheetResult.Canceled -> {
                PaymentResult.Cancelled
            }
            is PaymentSheetResult.Failed -> {
                PaymentResult.Error(
                    reason = result.error.localizedMessage ?: "Erreur de paiement",
                    code = "payment_failed"
                )
            }
        }
        resultCallback?.invoke(paymentResult)
    }
}

/**
 * Crée un PaymentSheetLauncher pour Android
 * Doit être appelé dans un Composable
 */
@Composable
actual fun rememberPaymentSheetLauncher(config: PaymentSheetConfig): PaymentSheetLauncher {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    return remember(config) {
        PaymentSheetLauncher(activity, config)
    }
}