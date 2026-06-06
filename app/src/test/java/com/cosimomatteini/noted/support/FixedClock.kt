package com.cosimomatteini.noted.support

import com.cosimomatteini.noted.domain.Clock
import java.time.Instant

class FixedClock(
    private val now: Instant,
) : Clock {
    override fun now(): Instant = now
}
