package org.society.appname.module.authentication

data class User(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val number: String? = null,
    val isEmailVerified: Boolean = false
)