package com.cosimomatteini.noted.ui

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri

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

internal fun Activity.reminderPermissionState(): ReminderPermissionState {
    val alarmManager = getSystemService(AlarmManager::class.java)
    val notificationPermissionRequested = getSharedPreferences(
        PERMISSIONS_PREFS,
        Context.MODE_PRIVATE
    ).getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false)

    return ReminderPermissionState(
        notificationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED,
        notificationCanRequest = !notificationPermissionRequested ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ),
        exactAlarmGranted = alarmManager.canScheduleExactAlarms()
    )
}

internal fun Context.markNotificationPermissionRequested() {
    getSharedPreferences(PERMISSIONS_PREFS, Context.MODE_PRIVATE)
        .edit {
            putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
        }
}

internal fun Context.openNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    )
}

internal fun Context.openExactAlarmSettings() {
    startActivity(
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            "package:$packageName".toUri()
        )
    )
}

private const val PERMISSIONS_PREFS = "permissions"
private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
