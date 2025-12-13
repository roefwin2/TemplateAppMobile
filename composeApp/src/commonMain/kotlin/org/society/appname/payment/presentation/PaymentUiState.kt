package org.society.appname.payment.presentation

data class PaymentUiState(
    val amountInput: String = "",
    val currency: String = "eur",
    val description: String = "",
    val customerEmail: String = "",
    val cardNumber: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cvc: String = "",
    val publishableKey: String = "",
    val clientSecret: String = "",
    val isProcessing: Boolean = false,
    val resultMessage: String? = null,
    val error: String? = null
)
