package com.todolist.app.data.local

import androidx.room.TypeConverter
import com.todolist.app.domain.model.Urgency
import java.time.LocalDate
import java.time.LocalTime

/** Converte tipos que o SQLite não conhece em tipos simples (número/texto). */
class Converters {
    @TypeConverter fun dateToLong(value: LocalDate?): Long? = value?.toEpochDay()
    @TypeConverter fun longToDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter fun timeToInt(value: LocalTime?): Int? = value?.toSecondOfDay()
    @TypeConverter fun intToTime(value: Int?): LocalTime? = value?.let { LocalTime.ofSecondOfDay(it.toLong()) }

    @TypeConverter fun urgencyToString(value: Urgency?): String? = value?.name
    @TypeConverter fun stringToUrgency(value: String?): Urgency? = value?.let { runCatching { Urgency.valueOf(it) }.getOrNull() }
}
