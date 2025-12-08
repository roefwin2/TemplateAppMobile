package org.society.appname.features.feedbacks

import androidx.compose.runtime.Composable

// Contexte neutre KMP : réel sur Android, vide sur iOS
expect class MailContext

// À appeler DANS un composable pour capturer le contexte (Android)
@Composable
expect fun rememberMailContext(): MailContext


expect fun openUserMail(
    ctx: MailContext,
    to: List<String>,
    subject: String? = null,
    body: String? = null
): Boolean