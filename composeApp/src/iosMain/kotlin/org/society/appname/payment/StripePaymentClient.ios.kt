package org.society.appname.payment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.society.appname.payment.domain.PaymentRepository
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult
import org.society.appname.payment.domain.ValidationResult
import platform.Foundation.NSLog

/**
 * iOS-specific Stripe configuration
 */
actual class StripePlatformConfig

/**
 * iOS implementation of Stripe payment client (stub)
 *
 * TODO: Implement using Stripe iOS SDK via CocoaPods/SPM
 */
actual class StripePaymentClient actual constructor(
    private val platformConfig: StripePlatformConfig
) : PaymentRepository {

    actual override suspend fun processPayment(request: PaymentRequest): PaymentResult =
        withContext(Dispatchers.Default) {

            NSLog("StripePaymentClient: Processing payment request")

            // Validate request first
            when (val validation = request.validate()) {
                is ValidationResult.Invalid -> {
                    return@withContext PaymentResult.Error(
                        reason = validation.firstError,
                        code = "validation_error"
                    )
                }
                is ValidationResult.Valid -> {
                    NSLog("StripePaymentClient: Request validation passed")
                }
            }

            NSLog("StripePaymentClient: iOS SDK not configured - returning stub error")

            return@withContext PaymentResult.Error(
                reason = "Stripe iOS n'est pas encore configuré. " +
                        "Veuillez intégrer le SDK Stripe iOS via CocoaPods ou SPM.",
                code = "ios_not_configured"
            )
        }
}