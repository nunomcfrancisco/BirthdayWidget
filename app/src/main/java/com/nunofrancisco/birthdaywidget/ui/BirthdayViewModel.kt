package com.nunofrancisco.birthdaywidget.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nunofrancisco.birthdaywidget.data.Birthday
import com.nunofrancisco.birthdaywidget.data.BirthdayRepository
import com.nunofrancisco.birthdaywidget.util.BirthdayCalculator
import com.nunofrancisco.birthdaywidget.util.UpcomingBirthday
import com.nunofrancisco.birthdaywidget.widget.WidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class BirthdayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BirthdayRepository.getInstance(application)

    val upcoming = repository.birthdays
        .map { list -> BirthdayCalculator.sortedByUpcoming(list, LocalDate.now()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<UpcomingBirthday>(),
        )

    fun addBirthday(name: String, day: Int, month: Int, year: Int?) {
        viewModelScope.launch {
            repository.add(
                Birthday(name = name.trim(), day = day, month = month, year = year)
            )
            WidgetUpdater.updateAll(getApplication())
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.delete(birthday)
            WidgetUpdater.updateAll(getApplication())
        }
    }
}
