package org.society.appname.payment.data

import org.society.appname.payment.StripePaymentClient
import org.society.appname.payment.domain.PaymentRepository
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult

class StripePaymentRepository(
    private val stripePaymentClient: StripePaymentClient
) : PaymentRepository {
    override suspend fun processPayment(request: PaymentRequest): PaymentResult {
        return stripePaymentClient.processPayment(request)
    }
}
