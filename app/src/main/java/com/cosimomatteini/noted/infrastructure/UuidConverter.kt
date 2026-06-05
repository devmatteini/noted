package com.cosimomatteini.noted.infrastructure

import androidx.room.TypeConverter
import java.util.UUID

class UuidConverter {
    @TypeConverter
    fun fromUuid(value: UUID): String = value.toString()

    @TypeConverter
    fun toUuid(value: String): UUID = UUID.fromString(value)
}
