package org.society.appname.payment.domain

/**
 * Result of a payment operation
 */
sealed class PaymentResult {

    /**
     * Payment completed successfully
     */
    data class Success(
        val message: String,
        val paymentIntentId: String? = null,
        val amount: Long? = null,
        val currency: String? = null
    ) : PaymentResult() {

        val formattedAmount: String?
            get() = if (amount != null && currency != null) {
                val amountDecimal = amount / 100.0
                "%.2f %s".plus(amountDecimal)
            } else null
    }

    /**
     * Payment requires additional authentication (3D Secure)
     */
    data class RequiresAction(
        val clientSecret: String,
        val paymentIntentId: String,
        val redirectUrl: String? = null
    ) : PaymentResult()

    /**
     * Payment was canceled
     */
    data object Cancelled : PaymentResult()

    /**
     * Payment failed with an error
     */
    data class Error(
        val reason: String,
        val code: String? = null,
        val declineCode: String? = null
    ) : PaymentResult() {

        /**
         * User-friendly error message in French
         */
        val userFriendlyMessage: String
            get() = when {
                declineCode != null -> getDeclineMessage(declineCode)
                code != null -> getCodeMessage(code)
                else -> reason
            }

        private fun getDeclineMessage(decline: String): String = when (decline) {
            "insufficient_funds" -> "Fonds insuffisants sur la carte"
            "lost_card" -> "Cette carte a été déclarée perdue"
            "stolen_card" -> "Cette carte a été déclarée volée"
            "expired_card" -> "La carte a expiré"
            "incorrect_cvc" -> "Le code CVC est incorrect"
            "incorrect_number" -> "Le numéro de carte est incorrect"
            "card_declined" -> "La carte a été refusée"
            "processing_error" -> "Erreur de traitement. Veuillez réessayer"
            "do_not_honor" -> "La banque a refusé la transaction"
            "try_again_later" -> "Veuillez réessayer plus tard"
            "card_not_supported" -> "Ce type de carte n'est pas supporté"
            "currency_not_supported" -> "Cette devise n'est pas supportée par la carte"
            "duplicate_transaction" -> "Transaction en double détectée"
            "fraudulent" -> "Transaction refusée pour suspicion de fraude"
            else -> "Carte refusée: $decline"
        }

        private fun getCodeMessage(errorCode: String): String = when (errorCode) {
            "card_declined" -> "La carte a été refusée"
            "expired_card" -> "La carte a expiré"
            "incorrect_cvc" -> "Le code CVC est incorrect"
            "incorrect_number" -> "Le numéro de carte est incorrect"
            "incorrect_zip" -> "Le code postal est incorrect"
            "invalid_card_type" -> "Ce type de carte n'est pas accepté"
            "invalid_expiry_month" -> "Le mois d'expiration est invalide"
            "invalid_expiry_year" -> "L'année d'expiration est invalide"
            "invalid_number" -> "Le numéro de carte est invalide"
            "postal_code_invalid" -> "Le code postal est invalide"
            "rate_limit" -> "Trop de requêtes. Veuillez patienter"
            "payment_intent_authentication_failure" -> "L'authentification a échoué"
            "payment_intent_payment_attempt_failed" -> "Le paiement a échoué"
            "payment_method_not_available" -> "Cette méthode de paiement n'est pas disponible"
            "authentication_error" -> "Erreur d'authentification Stripe"
            "network_error" -> "Erreur de connexion. Vérifiez votre connexion internet"
            "api_error" -> "Erreur du serveur Stripe. Veuillez réessayer"
            "validation_error" -> reason
            else -> reason
        }
    }

    // Helper properties
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val requiresAction: Boolean get() = this is RequiresAction
    val isCancelled: Boolean get() = this is Cancelled

    fun getSuccessOrNull(): Success? = this as? Success
    fun getErrorOrNull(): Error? = this as? Error

    /**
     * Execute action based on result type
     */
    inline fun fold(
        onSuccess: (Success) -> Unit,
        onError: (Error) -> Unit,
        onRequiresAction: (RequiresAction) -> Unit = {},
        onCancelled: () -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(this)
            is Error -> onError(this)
            is RequiresAction -> onRequiresAction(this)
            is Cancelled -> onCancelled()
        }
    }
}

/**
 * Extension to get a displayable message from any PaymentResult
 */
fun PaymentResult.getMessage(): String = when (this) {
    is PaymentResult.Success -> message
    is PaymentResult.Error -> userFriendlyMessage
    is PaymentResult.RequiresAction -> "Authentification 3D Secure requise"
    is PaymentResult.Cancelled -> "Paiement annulé"
}