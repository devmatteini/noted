package com.cosimomatteini.noted.support

import com.cosimomatteini.noted.domain.Logger

internal object EmptyLogger : Logger {
    override fun warn(tag: String, message: String, throwable: Throwable?) = Unit
}
