package com.cosimomatteini.noted.domain

@JvmInline
value class NoteDescription private constructor(val value: String) {
    companion object {
        fun of(value: String): NoteDescription = parse(value)

        fun parse(value: String): NoteDescription = NoteDescription(value.trim())
    }
}
