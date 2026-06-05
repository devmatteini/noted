package com.cosimomatteini.noted

import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.infrastructure.AndroidClock

class NotedAppContainer(
    val clock: Clock = AndroidClock(),
)
