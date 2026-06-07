package com.cosimomatteini.noted.domain

interface ReminderScheduler {
    fun schedule(noteId: NoteId, reminderAt: ReminderAt)

    fun cancel(noteId: NoteId)
}
