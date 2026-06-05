package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.Clock
import java.time.Instant

class AndroidClock : Clock {
    override fun now(): Instant = Instant.now()
}
