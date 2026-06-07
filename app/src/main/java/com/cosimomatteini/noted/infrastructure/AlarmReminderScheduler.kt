package com.cosimomatteini.noted.infrastructure

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.domain.ReminderScheduler

class AlarmReminderScheduler(context: Context) : ReminderScheduler {
    private val applicationContext = context.applicationContext
    private val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)

    override fun schedule(noteId: NoteId, reminderAt: ReminderAt) {
        if (!alarmManager.canScheduleExactAlarms()) return

        val pendingIntent = pendingIntent(noteId, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderAt.value.toEpochMilli(),
                pendingIntent
            )
        } catch (_: SecurityException) {
            pendingIntent.cancel()
        }
    }

    override fun cancel(noteId: NoteId) {
        val pendingIntent = pendingIntent(noteId, PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun pendingIntent(noteId: NoteId, flag: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            applicationContext,
            noteId.value.hashCode(),
            Intent(ACTION_SHOW_REMINDER)
                .setClassName(applicationContext.packageName, REMINDER_RECEIVER_CLASS_NAME)
                .putExtra(EXTRA_NOTE_ID, noteId.value.toString()),
            flag or PendingIntent.FLAG_IMMUTABLE
        )

    private companion object {
        const val ACTION_SHOW_REMINDER = "com.cosimomatteini.noted.SHOW_REMINDER"
        const val EXTRA_NOTE_ID = "note_id"
        const val REMINDER_RECEIVER_CLASS_NAME =
            "com.cosimomatteini.noted.infrastructure.ReminderReceiver"
    }
}
