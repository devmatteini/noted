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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(reminderNotificationText(note.title.value, note.description.value))
                .setContentIntent(contentIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
    }

    private fun contentIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        OPEN_APP_REQUEST_CODE,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private companion object {
        const val CHANNEL_ID = "reminders"
        const val OPEN_APP_REQUEST_CODE = 0
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

private const val MAX_DESCRIPTION_LENGTH = 100
private const val ELLIPSIS = "..."
