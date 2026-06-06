package com.cosimomatteini.noted.domain

@JvmInline
value class NoteTitle private constructor(val value: String) {
    companion object {
        fun of(value: String?): NoteTitle? = parse(value)

        fun parse(value: String?): NoteTitle? {
            val trimmed = value?.trim().orEmpty()
            if (trimmed.isEmpty()) {
                return null
            }
            return NoteTitle(trimmed)
        }
    }
}
