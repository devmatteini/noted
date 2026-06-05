package com.cosimomatteini.noted.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(UuidConverter::class)
abstract class NotedDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
