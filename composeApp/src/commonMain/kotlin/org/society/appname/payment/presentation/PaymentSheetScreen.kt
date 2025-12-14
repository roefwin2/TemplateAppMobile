package org.society.appname.payment.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.society.appname.payment.domain.PaymentResult

/**
 * Écran de paiement Compose Multiplatform avec Stripe PaymentSheet
 *
 * Usage:
 * ```kotlin
 * PaymentSheetScreen(
 *     publishableKey = "pk_test_xxx",
 *     clientSecret = "pi_xxx_secret_xxx",
 *     amount = 2999,
 *     currency = "EUR",
 *     merchantName = "Ma Boutique",
 *     productName = "Abonnement Premium",
 *     onPaymentSuccess = {
 *         navController.navigate("success")
 *     },
 *     onPaymentCancelled = { },
 *     onPaymentFailed = { error ->
 *         showSnackbar(error)
 *     }
 * )
 * ```
 */
@Composable
fun PaymentSheetScreen(
    publishableKey: String,
    clientSecret: String,
    amount: Long,
    currency: String = "EUR",
    merchantName: String = "Paiement",
    productName: String? = null,
    onPaymentSuccess: () -> Unit,
    onPaymentCancelled: () -> Unit = {},
    onPaymentFailed: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // État local
    var isProcessing by remember { mutableStateOf(false) }
    var paymentState by remember { mutableStateOf<PaymentState>(PaymentState.Idle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Créer le launcher PaymentSheet
    val paymentSheetLauncher = rememberPaymentSheetLauncher(
        config = PaymentSheetConfig(
            publishableKey = publishableKey,
            merchantName = merchantName
        )
    )

    // Format du montant
    val formattedAmount = remember(amount, currency) {
        "%.2f %s".plus(amount / 100.0)
    }

    // Fonction pour lancer le paiement
    fun launchPayment() {
        isProcessing = true
        errorMessage = null

        paymentSheetLauncher.present(clientSecret) { result ->
            isProcessing = false

            when (result) {
                is PaymentResult.Success -> {
                    paymentState = PaymentState.Success
                    onPaymentSuccess()
                }

                is PaymentResult.Cancelled -> {
                    paymentState = PaymentState.Idle
                    onPaymentCancelled()
                }

                is PaymentResult.Error -> {
                    paymentState = PaymentState.Error
                    errorMessage = result.userFriendlyMessage
                    onPaymentFailed?.invoke(result.userFriendlyMessage)
                }

                is PaymentResult.RequiresAction -> {
                    // Géré automatiquement par PaymentSheet
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Icône principale
            PaymentIcon(paymentState)

            Spacer(modifier = Modifier.height(32.dp))

            when (paymentState) {
                PaymentState.Success -> {
                    SuccessContent()
                }

                else -> {
                    // Titre
                    Text(
                        text = "Récapitulatif",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Carte récapitulative
                    OrderSummaryCard(
                        productName = productName,
                        formattedAmount = formattedAmount
                    )

                    // Message d'erreur
                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        ErrorCard(error)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Bouton de paiement
                    PayButton(
                        formattedAmount = formattedAmount,
                        isProcessing = isProcessing,
                        isReady = paymentSheetLauncher.isReady,
                        onClick = { launchPayment() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Badge sécurisé
                    SecureBadge()
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PaymentIcon(state: PaymentState) {
    val icon = when (state) {
        PaymentState.Success -> Icons.Default.Check
        else -> Icons.Default.ShoppingCart
    }

    val tint = when (state) {
        PaymentState.Success -> MaterialTheme.colorScheme.primary
        PaymentState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.1f),
        modifier = Modifier.size(100.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(24.dp)
                .size(52.dp),
            tint = tint
        )
    }
}

@Composable
private fun SuccessContent() {
    Text(
        text = "Paiement réussi !",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Merci pour votre achat",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OrderSummaryCard(
    productName: String?,
    formattedAmount: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            if (productName != null) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total à payer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PayButton(
    formattedAmount: String,
    isProcessing: Boolean,
    isReady: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isReady && !isProcessing,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Payer $formattedAmount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SecureBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Paiement sécurisé par Stripe",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * État interne du paiement
 */
private enum class PaymentState {
    Idle,
    Success,
    Error
}