package org.society.appname.features.feedbacks

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
actual class MailContext internal constructor(internal val context: Context)

@Composable
actual fun rememberMailContext(): MailContext =
    MailContext(LocalContext.current)

actual fun openUserMail(
    ctx: MailContext,
    to: List<String>,
    subject: String?,
    body: String?
): Boolean {
    val builder = Uri.parse("mailto:").buildUpon()
        .appendQueryParameter("to", to.joinToString(","))
    if (!subject.isNullOrBlank()) builder.appendQueryParameter("subject", subject)
    if (!body.isNullOrBlank()) builder.appendQueryParameter("body", body)
    val mailto = builder.build()

    val intent = Intent(Intent.ACTION_SENDTO, mailto)
    return try {
        ctx.context.startActivity(Intent.createChooser(intent, "Envoyer un e-mail"))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: Exception) {
        false
    }
}