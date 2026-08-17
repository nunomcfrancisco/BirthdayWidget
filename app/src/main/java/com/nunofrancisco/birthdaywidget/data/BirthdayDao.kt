package com.nunofrancisco.birthdaywidget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {

    @Query("SELECT * FROM birthdays ORDER BY month, day")
    fun observeAll(): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays ORDER BY month, day")
    suspend fun getAll(): List<Birthday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(birthday: Birthday): Long

    @Update
    suspend fun update(birthday: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)
}
