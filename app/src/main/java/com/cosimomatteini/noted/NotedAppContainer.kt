package com.cosimomatteini.noted

import android.content.Context
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.features.ArchiveNote
import com.cosimomatteini.noted.features.ClearNoteReminder
import com.cosimomatteini.noted.features.CreateEmptyNote
import com.cosimomatteini.noted.features.DeleteExpiredDiscardedNotes
import com.cosimomatteini.noted.features.DiscardNote
import com.cosimomatteini.noted.features.ExportNotes
import com.cosimomatteini.noted.features.Notes
import com.cosimomatteini.noted.features.PermanentlyDeleteNote
import com.cosimomatteini.noted.features.RestoreDiscardedNote
import com.cosimomatteini.noted.features.RestoreNote
import com.cosimomatteini.noted.features.SetNoteReminder
import com.cosimomatteini.noted.features.UpdateNote
import com.cosimomatteini.noted.infrastructure.AlarmReminderScheduler
import com.cosimomatteini.noted.infrastructure.AndroidClock
import com.cosimomatteini.noted.infrastructure.AndroidLogger
import com.cosimomatteini.noted.infrastructure.NotedDatabase
import com.cosimomatteini.noted.infrastructure.NotedDatabaseFactory
import com.cosimomatteini.noted.infrastructure.RoomNoteRepository
import com.cosimomatteini.noted.infrastructure.SharedPreferencesNotesLayoutPreference

class NotedAppContainer(context: Context, val clock: Clock = AndroidClock()) {
    private val database: NotedDatabase = NotedDatabaseFactory.create(context)

    val noteRepository: NoteRepository = RoomNoteRepository(database.noteDao(), AndroidLogger)
    private val reminderScheduler = AlarmReminderScheduler(context)
    val notesLayoutPreference = SharedPreferencesNotesLayoutPreference(context)
    val notes = Notes(noteRepository)
    val exportNotes = ExportNotes(noteRepository, clock)
    val createEmptyNote = CreateEmptyNote(noteRepository, clock)
    val updateNote = UpdateNote(noteRepository, clock)
    val deleteExpiredDiscardedNotes = DeleteExpiredDiscardedNotes(noteRepository, clock)
    val permanentlyDeleteNote = PermanentlyDeleteNote(noteRepository)
    val discardNote = DiscardNote(noteRepository, reminderScheduler, clock)
    val archiveNote = ArchiveNote(noteRepository, reminderScheduler, clock)
    val restoreNote = RestoreNote(noteRepository, clock)
    val restoreDiscardedNote = RestoreDiscardedNote(noteRepository, clock)
    val setNoteReminder = SetNoteReminder(noteRepository, reminderScheduler, clock)
    val clearNoteReminder = ClearNoteReminder(noteRepository, reminderScheduler, clock)
}
