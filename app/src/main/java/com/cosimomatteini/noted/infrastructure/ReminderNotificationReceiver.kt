package com.cosimomatteini.noted.infrastructure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.features.ArchiveNote
import com.cosimomatteini.noted.features.DiscardNote
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = ReminderAction.fromIntentAction(intent.action) ?: run {
            AndroidLogger.warn(
                ReminderNotificationReceiver::class.java.simpleName,
                "Ignoring unsupported reminder action: ${intent.action}"
            )
            return
        }

        val noteId = intent.getStringExtra(ReminderAlarm.EXTRA_NOTE_ID)
            ?.let { runCatching { NoteId(UUID.fromString(it)) }.getOrNull() }
            ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            val applicationContext = context.applicationContext
            val database = NotedDatabaseFactory.create(applicationContext)
            try {
                val noteRepository = RoomNoteRepository(
                    database.noteDao(),
                    AndroidLogger
                )
                val notification = ReminderNotification(applicationContext)

                when (action) {
                    ReminderAction.Show -> {
                        noteRepository.loadActive(noteId)?.let(notification::show)
                    }

                    ReminderAction.Discard -> {
                        DiscardNote(
                            noteRepository,
                            AlarmReminderScheduler(applicationContext),
                            AndroidClock()
                        )(noteId)
                        notification.cancel(noteId)
                    }

                    ReminderAction.Archive -> {
                        ArchiveNote(
                            noteRepository,
                            AlarmReminderScheduler(applicationContext),
                            AndroidClock()
                        )(noteId)
                        notification.cancel(noteId)
                    }
                }
            } finally {
                database.close()
                pendingResult.finish()
            }
        }
    }

    private enum class ReminderAction(val intentAction: String) {
        Show(ReminderAlarm.ACTION_SHOW_REMINDER),
        Discard(ReminderAlarm.ACTION_DISCARD_REMINDER),
        Archive(ReminderAlarm.ACTION_ARCHIVE_REMINDER);

        companion object {
            fun fromIntentAction(intentAction: String?): ReminderAction? =
                entries.firstOrNull { it.intentAction == intentAction }
        }
    }
}
