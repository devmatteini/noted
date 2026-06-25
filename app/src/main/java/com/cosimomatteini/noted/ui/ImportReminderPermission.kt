@file:Suppress("ktlint:standard:filename")

package com.cosimomatteini.noted.ui

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Note
import java.time.Instant

internal enum class ImportPermissionAction {
    Import,
    RequestNotification,
    OpenNotificationSettings,
    OpenExactAlarmSettings
}

internal fun nextImportPermissionAction(
    notes: List<Note>,
    now: Instant,
    state: ReminderPermissionState
): ImportPermissionAction {
    if (!notes.hasActiveFutureReminder(now)) return ImportPermissionAction.Import

    return when (nextReminderPermissionAction(state)) {
        ReminderPermissionAction.SaveReminder -> ImportPermissionAction.Import

        ReminderPermissionAction.RequestNotification -> ImportPermissionAction.RequestNotification

        ReminderPermissionAction.OpenNotificationSettings ->
            ImportPermissionAction.OpenNotificationSettings

        ReminderPermissionAction.OpenExactAlarmSettings ->
            ImportPermissionAction.OpenExactAlarmSettings
    }
}

private fun List<Note>.hasActiveFutureReminder(now: Instant): Boolean =
    filterIsInstance<ActiveNote>().any { note -> note.reminderAt?.value?.isAfter(now) == true }
