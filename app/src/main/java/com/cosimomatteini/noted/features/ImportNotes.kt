package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderScheduler

class ImportNotes(
    private val noteRepository: NoteRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke(notes: List<Note>): Result<ImportedNotes> = runCatching {
        noteRepository.saveAll(notes)

        notes.filterIsInstance<ActiveNote>().forEach { note ->
            reminderScheduler.cancel(note.id)
            val reminderAt = note.reminderAt ?: return@forEach
            if (reminderAt.value > clock.now()) {
                reminderScheduler.schedule(note.id, reminderAt)
            }
        }

        ImportedNotes(count = notes.size)
    }
}

data class ImportedNotes(val count: Int)
