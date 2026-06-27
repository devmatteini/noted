package com.cosimomatteini.noted.infrastructure

import android.content.Context
import androidx.core.content.edit
import com.cosimomatteini.noted.ui.NotesLayout
import com.cosimomatteini.noted.ui.NotesLayoutPreference

class SharedPreferencesNotesLayoutPreference(context: Context) : NotesLayoutPreference {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(): NotesLayout = when (preferences.getString(KEY_NOTES_LAYOUT, VALUE_LIST)) {
        VALUE_GRID -> NotesLayout.Grid
        else -> NotesLayout.List
    }

    override fun save(layout: NotesLayout) {
        preferences.edit {
            putString(KEY_NOTES_LAYOUT, layout.preferenceValue)
        }
    }

    private val NotesLayout.preferenceValue: String
        get() = when (this) {
            NotesLayout.List -> VALUE_LIST
            NotesLayout.Grid -> VALUE_GRID
        }

    private companion object {
        const val PREFERENCES_NAME = "noted_preferences"
        const val KEY_NOTES_LAYOUT = "notes_layout"
        const val VALUE_LIST = "list"
        const val VALUE_GRID = "grid"
    }
}
