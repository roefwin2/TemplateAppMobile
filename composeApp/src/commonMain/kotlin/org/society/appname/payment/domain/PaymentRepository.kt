package org.society.appname.payment.domain

interface PaymentRepository {
    suspend fun processPayment(request: PaymentRequest): PaymentResult
}
