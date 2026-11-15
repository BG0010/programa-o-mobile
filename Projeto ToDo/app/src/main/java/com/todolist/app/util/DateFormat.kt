package com.todolist.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")
private val ShortDate = DateTimeFormatter.ofPattern("d 'de' MMM", PtBr)
private val LongDate = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PtBr)
private val MonthYear = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PtBr)
private val WeekDayShort = DateTimeFormatter.ofPattern("EEE", PtBr)
private val Time = DateTimeFormatter.ofPattern("HH:mm", PtBr)

private fun String.capitalizeFirst() = replaceFirstChar { it.titlecase(PtBr) }

/** "13 de nov" */
fun LocalDate.shortLabel(): String = format(ShortDate).removeSuffix(".")

/** "Sexta-feira, 13 de novembro" */
fun LocalDate.longLabel(): String = format(LongDate).capitalizeFirst()

/** "Novembro de 2026" */
fun LocalDate.monthYearLabel(): String = format(MonthYear).capitalizeFirst()

/** "SEX" */
fun LocalDate.weekDayShortLabel(): String = format(WeekDayShort).removeSuffix(".").uppercase(PtBr)

fun LocalTime.label(): String = format(Time)

/** "Atrasada há 2 dias", "Vence hoje", "Vence amanhã", "Faltam 3 dias". */
fun relativeDueLabel(dueDate: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(today, dueDate)
    return when {
        days < -1 -> "Atrasada há ${-days} dias"
        days == -1L -> "Atrasada há 1 dia"
        days == 0L -> "Vence hoje"
        days == 1L -> "Vence amanhã"
        else -> "Faltam $days dias"
    }
}
