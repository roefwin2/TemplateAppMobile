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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.society.appname.payment.domain.PaymentResult

/**
 * Écran de paiement avec Stripe PaymentSheet
 *
 * L'utilisateur entre le montant, puis clique sur Payer
 * → La bottom sheet Stripe s'ouvre pour la saisie de carte
 */
@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    onPaymentSuccess: (() -> Unit)? = null,
    onPaymentFailed: ((String) -> Unit)? = null
) {
    // ===== CONFIGURATION STRIPE =====
    // TODO: Remplace par ta vraie publishable key
    val publishableKey = "pk_test_51RwKaADLhyHMaTl7..."  // ← Ta clé ici
    val merchantName = "Ma Boutique"

    // ===== ÉTAT LOCAL =====
    var amountInput by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("eur") }
    var description by remember { mutableStateOf("") }
    var clientSecret by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // ===== PAYMENT SHEET LAUNCHER =====
    val paymentSheetLauncher = rememberPaymentSheetLauncher(
        config = PaymentSheetConfig(
            publishableKey = publishableKey,
            merchantName = merchantName
        )
    )

    // Montant formaté
    val amount = amountInput.toLongOrNull() ?: 0L
    val formattedAmount = if (amount > 0) {
        "%.2f %s".plus(amount / 100.0)
    } else null

    // ===== FONCTION PAIEMENT =====
    fun launchPayment() {
        // Validation
        if (amount <= 0) {
            error = "Montant invalide"
            return
        }
        if (clientSecret.isBlank()) {
            error = "Client secret manquant"
            return
        }
        if (!clientSecret.contains("_secret_")) {
            error = "Format du client secret invalide"
            return
        }

        error = null
        resultMessage = null
        isProcessing = true

        // Lancer PaymentSheet
        paymentSheetLauncher.present(clientSecret) { result ->
            isProcessing = false

            when (result) {
                is PaymentResult.Success -> {
                    resultMessage = "✓ Paiement réussi !"
                    onPaymentSuccess?.invoke()
                }

                is PaymentResult.Cancelled -> {
                    // L'utilisateur a fermé la bottom sheet
                }

                is PaymentResult.Error -> {
                    error = result.userFriendlyMessage
                    onPaymentFailed?.invoke(result.userFriendlyMessage)
                }

                is PaymentResult.RequiresAction -> {
                    // Géré automatiquement par PaymentSheet
                }
            }
        }
    }

    // ===== UI =====
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Paiement",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Badge sécurisé
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Paiement sécurisé par Stripe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== MONTANT =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Montant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Montant (centimes)") },
                        placeholder = { Text("2999") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.lowercase().take(3) },
                        label = { Text("Devise") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                }

                // Affichage montant formaté
                formattedAmount?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total: $it",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ===== DESCRIPTION (optionnel) =====
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optionnel)") },
            modifier = Modifier.fillMaxWidth()
        )

        // ===== CLIENT SECRET =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configuration Stripe",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientSecret,
                    onValueChange = { clientSecret = it.trim() },
                    label = { Text("Client Secret (pi_xxx_secret_xxx)") },
                    placeholder = { Text("pi_xxx_secret_xxx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Obtenu via curl ou ton backend",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== BOUTON PAYER =====
        Button(
            onClick = { launchPayment() },
            enabled = paymentSheetLauncher.isReady && !isProcessing && amount > 0 && clientSecret.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
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
                    text = formattedAmount?.let { "Payer $it" } ?: "Payer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ===== MESSAGES =====
        resultMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        error?.let { errorMsg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✗ $errorMsg",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}