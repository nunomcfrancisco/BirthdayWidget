package com.nunofrancisco.birthdaywidget.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BirthdayWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = BirthdayWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Primeiro widget colocado: começa o agendamento diário à meia-noite.
        WidgetRefreshWorker.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Último widget removido: já não é preciso atualizar diariamente.
        WidgetRefreshWorker.cancel(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // A contagem de dias muda à meia-noite e após arranque/mudança de fuso;
        // nestes casos forçamos uma atualização de todas as instâncias e
        // (re)garantimos o agendamento diário.
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> {
                val pending = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        BirthdayWidget.refreshAll(appContext)
                        WidgetRefreshWorker.schedule(appContext)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
