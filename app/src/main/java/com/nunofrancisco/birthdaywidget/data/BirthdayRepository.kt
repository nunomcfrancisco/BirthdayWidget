package com.nunofrancisco.birthdaywidget.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Ponto único de acesso aos dados. É partilhado entre a app e o widget para que
 * ambos leiam da mesma base de dados Room.
 */
class BirthdayRepository(private val dao: BirthdayDao) {

    val birthdays: Flow<List<Birthday>> = dao.observeAll()

    suspend fun getAll(): List<Birthday> = dao.getAll()

    suspend fun add(birthday: Birthday): Long = dao.insert(birthday)

    suspend fun update(birthday: Birthday) = dao.update(birthday)

    suspend fun delete(birthday: Birthday) = dao.delete(birthday)

    companion object {
        @Volatile
        private var INSTANCE: BirthdayRepository? = null

        fun getInstance(context: Context): BirthdayRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BirthdayRepository(
                    BirthdayDatabase.getInstance(context).birthdayDao()
                ).also { INSTANCE = it }
            }
        }
    }
}
