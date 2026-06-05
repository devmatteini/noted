package com.cosimomatteini.noted.domain

@JvmInline
value class NoteDescription private constructor(val value: String) {
    companion object {
        fun create(value: String): NoteDescription {
            val trimmed = value.trim()
            require(trimmed.isNotEmpty()) { "Note description cannot be empty." }
            return NoteDescription(trimmed)
        }
    }
}
