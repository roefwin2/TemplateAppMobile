package org.society.appname.payment.domain

data class PaymentRequest(
    val amount: Long,
    val currency: String,
    val description: String?,
    val customerEmail: String?,
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvc: String,
    val publishableKey: String,
    val clientSecret: String
)
