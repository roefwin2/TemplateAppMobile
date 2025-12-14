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
                "%.2f %s".plus(amount / 100.0)
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
            else -> "Carte refusée: $decline"
        }

        private fun getCodeMessage(errorCode: String): String = when (errorCode) {
            "card_declined" -> "La carte a été refusée"
            "expired_card" -> "La carte a expiré"
            "incorrect_cvc" -> "Le code CVC est incorrect"
            "authentication_error" -> "Erreur d'authentification Stripe"
            "network_error" -> "Erreur de connexion"
            "api_error" -> "Erreur serveur. Veuillez réessayer"
            else -> reason
        }
    }

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isCancelled: Boolean get() = this is Cancelled
}