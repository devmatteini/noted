package com.cosimomatteini.noted

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.ui.HomeRoute
import com.cosimomatteini.noted.ui.HomeViewModel
import com.cosimomatteini.noted.ui.NoteEditorScreen
import com.cosimomatteini.noted.ui.ReminderPermissionAction
import com.cosimomatteini.noted.ui.ReminderPermissionState
import com.cosimomatteini.noted.ui.nextReminderPermissionAction
import com.cosimomatteini.noted.ui.theme.NotedTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: NotedAppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = NotedAppContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            NotedTheme {
                NotedApp(appContainer)
            }
        }
    }
}

@Composable
fun NotedApp(appContainer: NotedAppContainer) {
    val context = LocalContext.current
    var screen by remember { mutableStateOf<NotedScreen>(NotedScreen.Home) }
    var pendingReminder by remember { mutableStateOf<PendingReminder?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val reminder = pendingReminder
        if (granted && reminder != null) {
            pendingReminder = null
            handleReminderPermissionAction(
                context = context,
                noteId = reminder.noteId,
                reminderAt = reminder.reminderAt,
                appContainer = appContainer,
                coroutineScope = coroutineScope,
                requestNotificationPermission = { pendingReminder = reminder },
                openNotificationSettings = { context.openNotificationSettings() },
                openExactAlarmSettings = { context.openExactAlarmSettings() }
            )
        }
    }
    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(appContainer.notes) as T
        }
    )

    when (val currentScreen = screen) {
        NotedScreen.Home -> HomeRoute(
            viewModel = homeViewModel,
            onCreateNote = {
                coroutineScope.launch {
                    val note = appContainer.createEmptyNote()
                    screen = NotedScreen.EditNote(note)
                }
            },
            onEditNote = { note -> screen = NotedScreen.EditNote(note) }
        )

        is NotedScreen.EditNote -> NoteEditorScreen(
            initialTitle = currentScreen.note.title.value,
            initialDescription = currentScreen.note.description.value,
            initialReminderAt = currentScreen.note.reminderAt?.value,
            onAutosave = { title, description ->
                appContainer.updateNote(currentScreen.note.id, title, description)
            },
            onBack = { title, description ->
                appContainer.updateNote(currentScreen.note.id, title, description)
                screen = NotedScreen.Home
            },
            onArchive = { title, description ->
                appContainer.updateNote(currentScreen.note.id, title, description)
                appContainer.archiveNote(currentScreen.note.id)
                screen = NotedScreen.Home
            },
            onDelete = {
                appContainer.deleteNote(currentScreen.note.id)
                screen = NotedScreen.Home
            },
            onSetReminder = { reminderAt ->
                handleReminderPermissionAction(
                    context = context,
                    noteId = currentScreen.note.id,
                    reminderAt = reminderAt,
                    appContainer = appContainer,
                    coroutineScope = coroutineScope,
                    requestNotificationPermission = {
                        pendingReminder = PendingReminder(currentScreen.note.id, reminderAt)
                        context.markNotificationPermissionRequested()
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    },
                    openNotificationSettings = { context.openNotificationSettings() },
                    openExactAlarmSettings = { context.openExactAlarmSettings() }
                )
            },
            onClearReminder = {
                appContainer.clearNoteReminder(currentScreen.note.id)
            }
        )
    }
}

private fun handleReminderPermissionAction(
    context: Context,
    noteId: NoteId,
    reminderAt: ReminderAt,
    appContainer: NotedAppContainer,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    requestNotificationPermission: () -> Unit,
    openNotificationSettings: () -> Unit,
    openExactAlarmSettings: () -> Unit
): Boolean = when (nextReminderPermissionAction(context.reminderPermissionState())) {
    ReminderPermissionAction.SaveReminder -> coroutineScope.launch {
        appContainer.setNoteReminder(noteId, reminderAt)
    }.let { true }

    ReminderPermissionAction.RequestNotification -> {
        requestNotificationPermission()
        false
    }

    ReminderPermissionAction.OpenNotificationSettings -> {
        openNotificationSettings()
        false
    }

    ReminderPermissionAction.OpenExactAlarmSettings -> {
        openExactAlarmSettings()
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
                this as MainActivity,
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

private sealed interface NotedScreen {
    data object Home : NotedScreen

    data class EditNote(val note: ActiveNote) : NotedScreen
}

private data class PendingReminder(val noteId: NoteId, val reminderAt: ReminderAt)

private const val PERMISSIONS_PREFS = "permissions"
private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
