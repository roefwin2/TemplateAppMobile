package org.society.appname.features.feedbacks

import androidx.compose.runtime.Composable
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue


actual class MailContext // vide sur iOS

@Composable
actual fun rememberMailContext(): MailContext = MailContext()

private fun String.urlEncoded(): String {
    val nsString: NSString = NSString.create(string = this)
    val allowed = NSCharacterSet.characterSetWithCharactersInString(
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
    )
    return nsString.stringByAddingPercentEncodingWithAllowedCharacters(allowed) ?: this
}

actual fun openUserMail(
    ctx: MailContext,
    to: List<String>,
    subject: String?,
    body: String?
): Boolean {
    val urlStr = buildString {
        append("mailto:")
        append(to.joinToString(","))
        var first = true
        fun addParam(k: String, v: String?) {
            if (v.isNullOrBlank()) return
            append(if (first) "?" else "&")
            first = false
            append(k)
            append("=")
            append(v.urlEncoded())
        }
        addParam("subject", subject)
        addParam("body", body)
    }

    val url = NSURL.URLWithString(urlStr) ?: return false
    val app = UIApplication.sharedApplication
    if (!app.canOpenURL(url)) return false

    // ✅ API moderne (non-dépréciée) + completion handler
    // Optionnel: garantir le thread principal
    dispatch_async(
        dispatch_get_main_queue()
    ) {
        app.openURL(url, options = emptyMap<Any?, Any?>()) { _ -> }
    }
    return true
}