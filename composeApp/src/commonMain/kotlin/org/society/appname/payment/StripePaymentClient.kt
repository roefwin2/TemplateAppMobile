package org.society.appname.payment

import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult

expect class StripePlatformConfig

expect class StripePaymentClient(platformConfig: StripePlatformConfig) {
    suspend fun processPayment(request: PaymentRequest): PaymentResult
}
