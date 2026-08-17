package com.nunofrancisco.birthdaywidget.util

import com.nunofrancisco.birthdaywidget.data.Birthday
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A ocorrência seguinte de um aniversário, já resolvida para uma data concreta.
 */
data class UpcomingBirthday(
    val birthday: Birthday,
    val nextDate: LocalDate,
    val daysUntil: Long,
    /** Idade que a pessoa vai completar, ou null se o ano de nascimento é desconhecido. */
    val turningAge: Int?,
)

object BirthdayCalculator {

    /**
     * Devolve a próxima data em que este aniversário ocorre a partir de [today]
     * (inclusive). Trata o 29 de Fevereiro em anos não bissextos usando 28 de Fevereiro.
     */
    fun nextOccurrence(birthday: Birthday, today: LocalDate): LocalDate {
        var candidate = occurrenceInYear(birthday, today.year)
        if (candidate.isBefore(today)) {
            candidate = occurrenceInYear(birthday, today.year + 1)
        }
        return candidate
    }

    private fun occurrenceInYear(birthday: Birthday, year: Int): LocalDate {
        val day = if (birthday.month == 2 && birthday.day == 29 && !isLeapYear(year)) {
            28
        } else {
            birthday.day
        }
        return LocalDate.of(year, birthday.month, day)
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    fun toUpcoming(birthday: Birthday, today: LocalDate): UpcomingBirthday {
        val next = nextOccurrence(birthday, today)
        val days = ChronoUnit.DAYS.between(today, next)
        val age = birthday.year?.let { next.year - it }
        return UpcomingBirthday(birthday, next, days, age)
    }

    /**
     * Ordena todos os aniversários pela proximidade da próxima ocorrência.
     */
    fun sortedByUpcoming(birthdays: List<Birthday>, today: LocalDate): List<UpcomingBirthday> =
        birthdays.map { toUpcoming(it, today) }
            .sortedWith(compareBy({ it.daysUntil }, { it.birthday.name.lowercase() }))

    /**
     * O próximo aniversário a chegar, ou null se a lista está vazia.
     */
    fun next(birthdays: List<Birthday>, today: LocalDate): UpcomingBirthday? =
        sortedByUpcoming(birthdays, today).firstOrNull()
}
