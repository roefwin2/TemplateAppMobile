package org.society.appname.payment

import android.content.Context
import com.stripe.android.PaymentConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult

actual class StripePlatformConfig(val context: Context)

actual class StripePaymentClient actual constructor(
    private val platformConfig: StripePlatformConfig
) {
    private val dispatcher = Dispatchers.IO

    actual suspend fun processPayment(request: PaymentRequest): PaymentResult = withContext(dispatcher) {
        return@withContext try {
            PaymentConfiguration.init(platformConfig.context, request.publishableKey)

            if (request.clientSecret.isBlank()) {
                PaymentResult.Error("Client secret Stripe manquant")
            } else {
                val amountInUnit = request.amount / 100.0
                PaymentResult.Success(
                    "Stripe initialisé, PaymentIntent prêt (${request.currency.uppercase()} $amountInUnit)"
                )
            }
        } catch (e: Exception) {
            PaymentResult.Error("Erreur Stripe: ${e.message}")
        }
    }
}
