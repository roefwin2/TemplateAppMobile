package org.society.appname.payment

import android.content.Context
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
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
            val clientSecret = request.clientSecret
            if (clientSecret.isBlank()) return@withContext PaymentResult.Error("Client secret Stripe manquant")

            val stripe = Stripe(platformConfig.context, request.publishableKey)
            val paymentIntent = stripe.retrievePaymentIntentSynchronous(clientSecret)

            when {
                paymentIntent == null -> PaymentResult.Error("Impossible de récupérer le PaymentIntent")
                paymentIntent.status == com.stripe.android.model.StripeIntent.Status.RequiresPaymentMethod ->
                    PaymentResult.Error("PaymentIntent créé mais aucune méthode de paiement fournie")
                paymentIntent.status == com.stripe.android.model.StripeIntent.Status.Succeeded ->
                    PaymentResult.Success("Paiement réussi — PaymentIntent confirmé (${paymentIntent.currency?.uppercase()} ${paymentIntent.amount?.div(100.0)})")
                else -> PaymentResult.Success("PaymentIntent récupéré avec le statut ${paymentIntent.status?.code}")
            }
        } catch (e: Exception) {
            PaymentResult.Error("Erreur Stripe: ${e.message}")
        }
    }
}
