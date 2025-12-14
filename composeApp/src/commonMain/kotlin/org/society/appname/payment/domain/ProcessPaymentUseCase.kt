package org.society.appname.payment.domain

class ProcessPaymentUseCase(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(request: PaymentRequest): PaymentResult = repository.processPayment(request)
}
