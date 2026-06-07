package com.cosimomatteini.noted.ui

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.ReminderAt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun rememberSaveReminder(
    setReminder: suspend (NoteId, ReminderAt) -> Unit
): (SaveReminderRequest) -> Boolean {
    val context = LocalContext.current
    var pendingReminderSaveRequest by remember { mutableStateOf<SaveReminderRequest?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val request = pendingReminderSaveRequest
        if (granted && request != null) {
            pendingReminderSaveRequest = null
            saveReminder(
                context = context,
                request = request,
                coroutineScope = coroutineScope,
                setReminder = setReminder,
                requestNotificationPermission = { pendingReminderSaveRequest = it }
            )
        }
    }

    return { request ->
        saveReminder(
            context = context,
            request = request,
            coroutineScope = coroutineScope,
            setReminder = setReminder,
            requestNotificationPermission = {
                pendingReminderSaveRequest = it
                context.markNotificationPermissionRequested()
                reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }
}

private fun saveReminder(
    context: Context,
    request: SaveReminderRequest,
    coroutineScope: CoroutineScope,
    setReminder: suspend (NoteId, ReminderAt) -> Unit,
    requestNotificationPermission: (SaveReminderRequest) -> Unit
) = when (nextReminderPermissionAction(context.reminderPermissionState())) {
    ReminderPermissionAction.SaveReminder -> coroutineScope.launch {
        setReminder(request.noteId, request.reminderAt)
    }.let { true }

    ReminderPermissionAction.RequestNotification -> {
        requestNotificationPermission(request)
        false
    }

    ReminderPermissionAction.OpenNotificationSettings -> {
        context.openNotificationSettings()
        false
    }

    ReminderPermissionAction.OpenExactAlarmSettings -> {
        context.openExactAlarmSettings()
        false
    }
}

private fun Context.reminderPermissionState(): ReminderPermissionState {
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
                this as Activity,
                Manifest.permission.POST_NOTIFICATIONS
            ),
        exactAlarmGranted = alarmManager.canScheduleExactAlarms()
    )
}

private fun Context.markNotificationPermissionRequested() {
    getSharedPreferences(PERMISSIONS_PREFS, Context.MODE_PRIVATE)
        .edit {
            putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
        }
}

private fun Context.openNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    )
}

private fun Context.openExactAlarmSettings() {
    startActivity(
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            "package:$packageName".toUri()
        )
    )
}

private const val PERMISSIONS_PREFS = "permissions"
private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
