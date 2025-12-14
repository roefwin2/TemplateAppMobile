package org.society.appname.payment

import org.society.appname.payment.domain.PaymentRepository
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult

/**
 * Platform-specific configuration for Stripe
 * - Android: requires Context
 * - iOS: empty
 */
expect class StripePlatformConfig

/**
 * Stripe payment client for processing payments
 * Implements PaymentRepository for use with ProcessPaymentUseCase
 */
expect class StripePaymentClient(platformConfig: StripePlatformConfig) : PaymentRepository {
    override suspend fun processPayment(request: PaymentRequest): PaymentResult
}