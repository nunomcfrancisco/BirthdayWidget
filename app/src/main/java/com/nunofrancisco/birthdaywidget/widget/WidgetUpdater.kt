package com.nunofrancisco.birthdaywidget.widget

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Utilitário para atualizar todas as instâncias do widget — usado sempre que a
 * lista de aniversários muda na app.
 *
 * As atualizações correm num scope próprio (não ligado ao ciclo de vida do
 * ViewModel ou da Activity), para garantir que terminam mesmo que o ecrã seja
 * fechado logo a seguir a adicionar/apagar um aniversário.
 */
object WidgetUpdater {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Versão suspensa, para quem já está dentro de uma coroutine (ex.: o receiver). */
    suspend fun updateAll(context: Context) {
        BirthdayWidget.refreshAll(context.applicationContext)
    }

    /** Dispara a atualização de forma independente e à prova de cancelamento. */
    fun requestUpdate(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            runCatching { BirthdayWidget.refreshAll(appContext) }
        }
    }
}
