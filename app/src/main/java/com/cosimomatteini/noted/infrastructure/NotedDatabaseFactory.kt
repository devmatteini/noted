package com.cosimomatteini.noted.infrastructure

import android.content.Context
import androidx.room.Room

object NotedDatabaseFactory {
    fun create(context: Context): NotedDatabase = Room.databaseBuilder(
        context.applicationContext,
        NotedDatabase::class.java,
        DATABASE_NAME
    ).build()

    private const val DATABASE_NAME = "noted.db"
}
