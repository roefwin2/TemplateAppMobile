package org.society.appname.payment.domain

/**
 * Request data for processing a Stripe payment
 */
data class PaymentRequest(
    val amount: Long,
    val currency: String = "eur",
    val description: String? = null,
    val customerEmail: String? = null,
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvc: String,
    val publishableKey: String,
    val clientSecret: String,
    val cardholderName: String? = null
) {
    /**
     * Validates the payment request
     * @param skipLuhnCheck Set to true to skip card number checksum validation (Stripe validates anyway)
     */
    fun validate(skipLuhnCheck: Boolean = true): ValidationResult {
        val errors = mutableListOf<String>()

        if (amount <= 0) {
            errors.add("Le montant doit être supérieur à 0")
        }

        if (publishableKey.isBlank()) {
            errors.add("Clé publique Stripe manquante")
        } else if (!publishableKey.startsWith("pk_")) {
            errors.add("Clé publique Stripe invalide (doit commencer par 'pk_')")
        }

        if (clientSecret.isBlank()) {
            errors.add("Client secret manquant")
        } else if (!clientSecret.contains("_secret_")) {
            errors.add("Format du client secret invalide")
        }

        val cleanedNumber = cleanedCardNumber()
        if (cleanedNumber.length < 13 || cleanedNumber.length > 19) {
            errors.add("Numéro de carte invalide (13-19 chiffres requis)")
        } else if (!cleanedNumber.all { it.isDigit() }) {
            errors.add("Le numéro de carte ne doit contenir que des chiffres")
        } else if (!skipLuhnCheck && !isValidLuhn(cleanedNumber)) {
            errors.add("Numéro de carte invalide")
        }

        if (expiryMonth < 1 || expiryMonth > 12) {
            errors.add("Mois d'expiration invalide (1-12)")
        }

        if (expiryYear < 2024 || expiryYear > 2050) {
            errors.add("Année d'expiration invalide")
        }

        if (cvc.length < 3 || cvc.length > 4 || !cvc.all { it.isDigit() }) {
            errors.add("CVC invalide (3-4 chiffres)")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Returns a cleaned card number (no spaces or dashes)
     */
    fun cleanedCardNumber(): String = cardNumber
        .replace(" ", "")
        .replace("-", "")
        .replace(".", "")
        .filter { it.isDigit() }

    /**
     * Detects the card brand based on the card number
     */
    fun detectCardBrand(): CardBrand {
        val number = cleanedCardNumber()
        return when {
            number.startsWith("4") -> CardBrand.VISA
            number.startsWith("51") || number.startsWith("52") ||
                    number.startsWith("53") || number.startsWith("54") ||
                    number.startsWith("55") || (number.length >= 2 && number.startsWith("2") &&
                    number.substring(0, 2).toIntOrNull()?.let { it in 22..27 } == true) -> CardBrand.MASTERCARD
            number.startsWith("34") || number.startsWith("37") -> CardBrand.AMEX
            number.startsWith("6011") || number.startsWith("65") -> CardBrand.DISCOVER
            number.startsWith("36") || number.startsWith("38") -> CardBrand.DINERS
            number.startsWith("35") -> CardBrand.JCB
            number.startsWith("62") -> CardBrand.UNIONPAY
            else -> CardBrand.UNKNOWN
        }
    }

    private fun isValidLuhn(number: String): Boolean {
        if (number.isEmpty() || number.any { !it.isDigit() }) return false

        var sum = 0
        var isSecond = false

        for (i in number.length - 1 downTo 0) {
            var digit = number[i].digitToInt()

            if (isSecond) {
                digit *= 2
                if (digit > 9) digit -= 9
            }

            sum += digit
            isSecond = !isSecond
        }

        return (sum % 10) == 0
    }
}

/**
 * Card brand detection
 */
enum class CardBrand(val displayName: String, val icon: String) {
    VISA("Visa", "💳"),
    MASTERCARD("Mastercard", "💳"),
    AMEX("American Express", "💳"),
    DISCOVER("Discover", "💳"),
    DINERS("Diners Club", "💳"),
    JCB("JCB", "💳"),
    UNIONPAY("UnionPay", "💳"),
    UNKNOWN("Carte", "💳")
}

/**
 * Result of payment request validation
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult() {
        val firstError: String get() = errors.firstOrNull() ?: "Erreur de validation"
        val allErrors: String get() = errors.joinToString("\n")
    }
}