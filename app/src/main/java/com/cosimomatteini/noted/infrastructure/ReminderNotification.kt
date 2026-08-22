package com.cosimomatteini.noted.infrastructure

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cosimomatteini.noted.MainActivity
import com.cosimomatteini.noted.R
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteId

class ReminderNotification(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun show(note: ActiveNote) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        notificationManager.notify(
            note.id.value.hashCode(),
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(reminderNotificationText(note.title.value, note.description.value))
                .setContentIntent(contentIntent(note))
                .addAction(
                    0,
                    context.getString(R.string.reminder_notification_action_done),
                    actionIntent(note, ReminderAlarm.ACTION_DISCARD_REMINDER)
                )
                .addAction(
                    0,
                    context.getString(R.string.reminder_notification_action_archive),
                    actionIntent(note, ReminderAlarm.ACTION_ARCHIVE_REMINDER)
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
    }

    fun cancel(noteId: NoteId) {
        notificationManager.cancel(noteId.value.hashCode())
    }

    private fun contentIntent(note: ActiveNote): PendingIntent = PendingIntent.getActivity(
        context,
        note.id.value.hashCode(),
        Intent(context, MainActivity::class.java)
            .putExtra(ReminderAlarm.EXTRA_NOTE_ID, note.id.value.toString())
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun actionIntent(note: ActiveNote, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            note.id.value.hashCode(),
            Intent(context, ReminderNotificationReceiver::class.java)
                .setAction(action)
                .putExtra(ReminderAlarm.EXTRA_NOTE_ID, note.id.value.toString()),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private companion object {
        const val CHANNEL_ID = "reminders"
    }
}

internal fun reminderNotificationText(title: String, description: String): String {
    val ellipsizedDescription = description.ellipsize(MAX_DESCRIPTION_LENGTH)

    return when {
        title.isNotEmpty() && ellipsizedDescription.isNotEmpty() ->
            "$title - $ellipsizedDescription"

        title.isNotEmpty() -> title

        ellipsizedDescription.isNotEmpty() -> ellipsizedDescription

        else -> "Reminder"
    }
}

private fun String.ellipsize(maxLength: Int): String = if (length <= maxLength) {
    this
} else {
    take(maxLength - ELLIPSIS.length) + ELLIPSIS
}

private const val MAX_DESCRIPTION_LENGTH = 500
private const val ELLIPSIS = "..."
