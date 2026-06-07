package com.cosimomatteini.noted.support

import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.domain.ReminderScheduler

class InMemoryReminderScheduler : ReminderScheduler {
    val scheduled = mutableListOf<Pair<NoteId, ReminderAt>>()
    val cancelled = mutableListOf<NoteId>()

    override fun schedule(noteId: NoteId, reminderAt: ReminderAt) {
        scheduled += noteId to reminderAt
    }

    override fun cancel(noteId: NoteId) {
        cancelled += noteId
    }
}
