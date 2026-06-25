package com.cosimomatteini.noted.ui

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.ReminderAt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun rememberSaveReminder(
    activity: Activity,
    setReminder: suspend (NoteId, ReminderAt) -> Unit
): (SaveReminderRequest) -> Boolean {
    var pendingReminderSaveRequest by remember { mutableStateOf<SaveReminderRequest?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val request = pendingReminderSaveRequest
        if (granted && request != null) {
            pendingReminderSaveRequest = null
            saveReminder(
                activity = activity,
                request = request,
                coroutineScope = coroutineScope,
                setReminder = setReminder,
                requestNotificationPermission = { pendingReminderSaveRequest = it }
            )
        }
    }

    return { request ->
        saveReminder(
            activity = activity,
            request = request,
            coroutineScope = coroutineScope,
            setReminder = setReminder,
            requestNotificationPermission = {
                pendingReminderSaveRequest = it
                activity.markNotificationPermissionRequested()
                reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }
}

private fun saveReminder(
    activity: Activity,
    request: SaveReminderRequest,
    coroutineScope: CoroutineScope,
    setReminder: suspend (NoteId, ReminderAt) -> Unit,
    requestNotificationPermission: (SaveReminderRequest) -> Unit
) = when (nextReminderPermissionAction(activity.reminderPermissionState())) {
    ReminderPermissionAction.SaveReminder -> coroutineScope.launch {
        setReminder(request.noteId, request.reminderAt)
    }.let { true }

    ReminderPermissionAction.RequestNotification -> {
        requestNotificationPermission(request)
        false
    }

    ReminderPermissionAction.OpenNotificationSettings -> {
        activity.openNotificationSettings()
        false
    }

    ReminderPermissionAction.OpenExactAlarmSettings -> {
        activity.openExactAlarmSettings()
        false
    }
}
