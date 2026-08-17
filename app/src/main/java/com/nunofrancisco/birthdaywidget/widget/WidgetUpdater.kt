package com.nunofrancisco.birthdaywidget.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/**
 * Utilitário para forçar a atualização de todas as instâncias do widget — usado
 * sempre que a lista de aniversários muda na app.
 */
object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        BirthdayWidget().updateAll(context)
    }
}
