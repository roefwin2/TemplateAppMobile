package org.society.appname.core.helpers

import dev.gitlive.firebase.firestore.DocumentSnapshot

 inline fun <reified T> DocumentSnapshot.getOrNull(field: String): T? =
    runCatching { get<T>(field) }.getOrNull()

fun DocumentSnapshot.getLongOrNull(field: String): Long? = runCatching {
    when (val v = get<Any>(field)) {
        is Long -> v
        is Int -> v.toLong()
        is Double -> v.toLong()
        else -> null
    }
}.getOrNull()