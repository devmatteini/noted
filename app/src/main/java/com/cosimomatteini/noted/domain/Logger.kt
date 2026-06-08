package com.cosimomatteini.noted.domain

interface Logger {
    fun warn(tag: String, message: String, throwable: Throwable?)

    fun warn(tag: String, message: String) {
        warn(tag, message, null)
    }
}
