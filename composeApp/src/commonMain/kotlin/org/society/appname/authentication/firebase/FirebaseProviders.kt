package org.society.appname.authentication.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth

object FirebaseProviders {
    val auth: FirebaseAuth by lazy { Firebase.auth }
}