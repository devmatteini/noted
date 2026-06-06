package com.cosimomatteini.noted.ui

internal data class ReminderPermissionState(
    val notificationGranted: Boolean,
    val notificationCanRequest: Boolean,
    val exactAlarmGranted: Boolean
)

internal enum class ReminderPermissionAction {
    SaveReminder,
    RequestNotification,
    OpenNotificationSettings,
    OpenExactAlarmSettings
}

internal fun nextReminderPermissionAction(
    state: ReminderPermissionState
): ReminderPermissionAction = when {
    !state.notificationGranted && state.notificationCanRequest ->
        ReminderPermissionAction.RequestNotification

    !state.notificationGranted -> ReminderPermissionAction.OpenNotificationSettings
    !state.exactAlarmGranted -> ReminderPermissionAction.OpenExactAlarmSettings
    else -> ReminderPermissionAction.SaveReminder
}
