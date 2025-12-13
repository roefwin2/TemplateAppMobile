package org.society.appname.payment

import android.content.Context
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.society.appname.payment.domain.PaymentRepository
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult
import org.society.appname.payment.domain.ValidationResult

/**
 * Android-specific Stripe configuration
 */
actual class StripePlatformConfig(val context: Context)

/**
 * Android implementation of Stripe payment client
 */
actual class StripePaymentClient actual constructor(
    private val platformConfig: StripePlatformConfig
) : PaymentRepository {

    private val dispatcher = Dispatchers.IO

   actual override suspend fun processPayment(request: PaymentRequest): PaymentResult = withContext(dispatcher) {
        try {
            // Validate request first
            when (val validation = request.validate()) {
                is ValidationResult.Invalid -> {
                    return@withContext PaymentResult.Error(
                        reason = validation.firstError,
                        code = "validation_error"
                    )
                }
                is ValidationResult.Valid -> { /* Continue */ }
            }

            // Initialize Stripe
            PaymentConfiguration.init(platformConfig.context, request.publishableKey)
            val stripe = Stripe(platformConfig.context, request.publishableKey)

            // Step 1: Create PaymentMethod first
            val cardParams = PaymentMethodCreateParams.Card.Builder()
                .setNumber(request.cleanedCardNumber())
                .setExpiryMonth(request.expiryMonth)
                .setExpiryYear(request.expiryYear)
                .setCvc(request.cvc)
                .build()

            val paymentMethodParams = PaymentMethodCreateParams.create(card = cardParams)

            val paymentMethod = stripe.createPaymentMethodSynchronous(paymentMethodParams)
                ?: return@withContext PaymentResult.Error(
                    reason = "Impossible de créer la méthode de paiement",
                    code = "payment_method_creation_failed"
                )

            // Step 2: Confirm PaymentIntent with the PaymentMethod ID
            val confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodId(
                paymentMethodId = paymentMethod.id!!,
                clientSecret = request.clientSecret
            )

            val paymentIntent = stripe.confirmPaymentIntentSynchronous(confirmParams)

            // Handle result
            when {
                paymentIntent == null -> {
                    PaymentResult.Error(
                        reason = "Impossible de confirmer le paiement",
                        code = "null_payment_intent"
                    )
                }

                paymentIntent.status == StripeIntent.Status.Succeeded -> {
                    PaymentResult.Success(
                        message = "Paiement réussi",
                        paymentIntentId = paymentIntent.id,
                        amount = paymentIntent.amount,
                        currency = paymentIntent.currency
                    )
                }

                paymentIntent.status == StripeIntent.Status.RequiresAction -> {
                    PaymentResult.RequiresAction(
                        clientSecret = request.clientSecret,
                        paymentIntentId = paymentIntent.id ?: "",
                        redirectUrl = null
                    )
                }

                paymentIntent.status == StripeIntent.Status.RequiresPaymentMethod -> {
                    val lastError = paymentIntent.lastPaymentError
                    PaymentResult.Error(
                        reason = lastError?.message ?: "La carte a été refusée",
                        code = lastError?.code,
                        declineCode = lastError?.declineCode
                    )
                }

                paymentIntent.status == StripeIntent.Status.Canceled -> {
                    PaymentResult.Cancelled
                }

                paymentIntent.status == StripeIntent.Status.Processing -> {
                    PaymentResult.Success(
                        message = "Paiement en cours de traitement",
                        paymentIntentId = paymentIntent.id,
                        amount = paymentIntent.amount,
                        currency = paymentIntent.currency
                    )
                }

                else -> {
                    PaymentResult.Success(
                        message = "Statut: ${paymentIntent.status?.code}",
                        paymentIntentId = paymentIntent.id,
                        amount = paymentIntent.amount,
                        currency = paymentIntent.currency
                    )
                }
            }

        } catch (e: Exception) {
            val message = e.message ?: "Erreur inconnue"
            PaymentResult.Error(
                reason = "Erreur: $message",
                code = "stripe_error"
            )
        }
    }
}