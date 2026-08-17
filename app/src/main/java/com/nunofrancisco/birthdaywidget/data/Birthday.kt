package com.nunofrancisco.birthdaywidget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Um aniversário guardado pelo utilizador.
 *
 * O ano é opcional porque nem sempre se sabe o ano de nascimento; quando existe
 * é usado para calcular a idade que a pessoa vai fazer.
 */
@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val month: Int,
    val day: Int,
    val year: Int? = null,
)
