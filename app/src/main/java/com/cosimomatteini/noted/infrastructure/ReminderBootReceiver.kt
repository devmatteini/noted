package com.cosimomatteini.noted.infrastructure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cosimomatteini.noted.features.RestoreReminders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val database = NotedDatabaseFactory.create(context)
            try {
                RestoreReminders(
                    noteRepository = RoomNoteRepository(database.noteDao()),
                    reminderScheduler = AlarmReminderScheduler(context.applicationContext),
                    clock = AndroidClock()
                )()
            } finally {
                database.close()
                pendingResult.finish()
            }
        }
    }
}
