package com.cosimomatteini.noted.ui

import android.content.Context
import android.content.Intent

internal fun formatNoteShareText(title: String, description: String): String = when {
    title.isBlank() -> description
    description.isBlank() -> title
    else -> "$title\n$description"
}

internal fun Context.shareNote(title: String, description: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, formatNoteShareText(title, description))
    }
    startActivity(Intent.createChooser(shareIntent, null))
}
