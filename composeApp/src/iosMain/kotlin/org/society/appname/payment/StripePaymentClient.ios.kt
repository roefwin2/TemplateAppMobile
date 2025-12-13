package org.society.appname.payment

import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult

actual class StripePlatformConfig

actual class StripePaymentClient actual constructor(
    private val platformConfig: StripePlatformConfig
) {
    actual suspend fun processPayment(request: PaymentRequest): PaymentResult {
        return PaymentResult.Error("Stripe iOS n'est pas configuré dans ce template")
    }
}
