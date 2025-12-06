package org.society.appname

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform