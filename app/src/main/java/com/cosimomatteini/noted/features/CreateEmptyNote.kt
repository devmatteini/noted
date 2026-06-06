package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository

class CreateEmptyNote(
    private val noteRepository: NoteRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): ActiveNote {
        val note = ActiveNote.empty(clock.now())

        noteRepository.save(note)
        return note
    }
}
