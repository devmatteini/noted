package com.cosimomatteini.noted.infrastructure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteId
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderAlarm.ACTION_SHOW_REMINDER) return

        val noteId = intent.getStringExtra(ReminderAlarm.EXTRA_NOTE_ID)
            ?.let { runCatching { NoteId(UUID.fromString(it)) }.getOrNull() }
            ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            val database = NotedDatabaseFactory.create(context)
            try {
                val note = RoomNoteRepository(
                    database.noteDao(),
                    AndroidLogger
                ).load(noteId) as? ActiveNote
                if (note != null) {
                    ReminderNotification(context.applicationContext).show(note)
                }
            } finally {
                database.close()
                pendingResult.finish()
            }
        }
    }
}
