package com.cosimomatteini.noted.ui

import com.cosimomatteini.noted.domain.ReminderAt
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun ReminderAt.formatReminderDateTime(): String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(
        value.atZone(ZoneId.systemDefault())
    )

internal fun LocalDate.formatReminderDialogDate(): String =
    DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()).format(this)

internal fun LocalTime.formatReminderDialogTime(): String =
    DateTimeFormatter.ofPattern("HH:mm").format(this)
