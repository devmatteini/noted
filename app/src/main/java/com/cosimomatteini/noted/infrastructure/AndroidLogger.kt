package com.cosimomatteini.noted.infrastructure

import android.util.Log
import com.cosimomatteini.noted.domain.Logger

object AndroidLogger : Logger {
    override fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }
}
