package org.society.appname.payment.presentation

import org.society.appname.payment.domain.CardBrand

data class PaymentUiState(
    // Amount & payment info
    val amountInput: String = "2999",
    val currency: String = "eur",
    val description: String = "",
    val customerEmail: String = "",

    // Card details
    val cardNumber: String = "4242424242424242",
    val cardNumberFormatted: String = "",
    val cardBrand: CardBrand = CardBrand.UNKNOWN,
    val expiryMonth: String = "12",
    val expiryYear: String = "2026",
    val cvc: String = "123",
    val cardholderName: String = "",

    // Stripe configuration
    val publishableKey: String = "pk_test_51RwKaADLhyHMaTl7IVRZPhz23TMidWmqwVB4dklzU9OUhTajaHsFQnmb75caTkxa95pw3P7LUrVsA9WyLKlB8bDX000v6aj4e0",
    val clientSecret: String = "pi_3Se2QVDLhyHMaTl70sTrM31L_secret_FmtacQrt8VcOP4KMiLRBi4PzU",

    // State
    val isProcessing: Boolean = false,
    val resultMessage: String? = null,
    val error: String? = null,

    // Field errors for inline validation
    val cardNumberError: String? = null,
    val expiryError: String? = null,
    val cvcError: String? = null,
    val amountError: String? = null
) {
    /**
     * Check if the form has valid data
     */
    val isValid: Boolean
        get() = amountInput.toLongOrNull()?.let { it > 0 } == true &&
                cardNumber.length >= 13 &&
                expiryMonth.toIntOrNull()?.let { it in 1..12 } == true &&
                expiryYear.toIntOrNull()?.let { it >= 2024 } == true &&
                cvc.length >= 3 &&
                publishableKey.isNotBlank() &&
                clientSecret.isNotBlank()

    /**
     * Formatted amount for display
     */
    val formattedAmount: String?
        get() = amountInput.toLongOrNull()?.let { cents ->
            "%.2f %s".plus(cents / 100.0)
        }

    /**
     * Card brand display name
     */
    val cardBrandDisplay: String
        get() = if (cardNumber.length >= 2) cardBrand.displayName else ""
}