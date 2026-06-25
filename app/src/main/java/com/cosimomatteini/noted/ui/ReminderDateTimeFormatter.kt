package com.cosimomatteini.noted.ui

import android.icu.text.RelativeDateTimeFormatter
import com.cosimomatteini.noted.domain.ReminderAt
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal fun ReminderAt.formatReminderChipDateTime(): String {
    val zoneId = ZoneId.systemDefault()
    val locale = Locale.getDefault()
    val reminderDateTime = value.atZone(zoneId)
    val reminderDate = reminderDateTime.toLocalDate()
    val today = LocalDate.now(zoneId)
    val date = when (reminderDate) {
        today -> relativeDay(RelativeDateTimeFormatter.Direction.THIS, locale)

        today.plusDays(1) -> relativeDay(RelativeDateTimeFormatter.Direction.NEXT, locale)

        else ->
            DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .format(reminderDate)
    }
    val time = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(locale)
        .format(reminderDateTime)

    return "$date, $time"
}

internal fun ReminderAt.formatCompactReminderChipDateTime(): String {
    val zoneId = ZoneId.systemDefault()
    val locale = Locale.getDefault()
    val reminderDateTime = value.atZone(zoneId)
    val reminderDate = reminderDateTime.toLocalDate()
    val today = LocalDate.now(zoneId)
    val date = when (reminderDate) {
        today -> relativeDay(RelativeDateTimeFormatter.Direction.THIS, locale)

        today.plusDays(1) -> relativeDay(RelativeDateTimeFormatter.Direction.NEXT, locale)

        else ->
            DateTimeFormatter
                .ofPattern(if (reminderDate.year == today.year) "d MMM" else "d MMM yyyy", locale)
                .format(reminderDate)
    }
    val time = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(locale)
        .format(reminderDateTime)

    return "$date, $time"
}

private fun relativeDay(direction: RelativeDateTimeFormatter.Direction, locale: Locale): String =
    RelativeDateTimeFormatter
        .getInstance(locale)
        .format(direction, RelativeDateTimeFormatter.AbsoluteUnit.DAY)
        .replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) {
                firstChar.titlecase(locale)
            } else {
                firstChar.toString()
            }
        }

internal fun LocalDate.formatReminderDialogDate(): String =
    DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()).format(this)

internal fun LocalTime.formatReminderDialogTime(): String =
    DateTimeFormatter.ofPattern("HH:mm").format(this)
