package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class DeleteNote(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(id: NoteId) {
        noteRepository.delete(id)
    }
}
