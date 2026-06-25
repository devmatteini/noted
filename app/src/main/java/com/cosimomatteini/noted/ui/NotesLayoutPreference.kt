package com.cosimomatteini.noted.ui

enum class NotesLayout {
    List,
    Grid
}

interface NotesLayoutPreference {
    fun load(): NotesLayout

    fun save(layout: NotesLayout)
}
