package com.cosimomatteini.noted.domain

import java.time.Instant

interface Clock {
    fun now(): Instant
}
