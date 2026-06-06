package com.cosimomatteini.noted

import android.content.Context
import androidx.room.Room
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.features.CreateEmptyNote
import com.cosimomatteini.noted.features.DeleteNote
import com.cosimomatteini.noted.features.Notes
import com.cosimomatteini.noted.features.UpdateNote
import com.cosimomatteini.noted.infrastructure.AndroidClock
import com.cosimomatteini.noted.infrastructure.NotedDatabase
import com.cosimomatteini.noted.infrastructure.RoomNoteRepository

class NotedAppContainer(
    context: Context,
    val clock: Clock = AndroidClock(),
) {
    private val database: NotedDatabase = Room.databaseBuilder(
        context,
        NotedDatabase::class.java,
        "noted.db",
    ).build()

    val noteRepository: NoteRepository = RoomNoteRepository(database.noteDao())
    val notes = Notes(noteRepository)
    val createEmptyNote = CreateEmptyNote(noteRepository, clock)
    val updateNote = UpdateNote(noteRepository, clock)
    val deleteNote = DeleteNote(noteRepository)
}
