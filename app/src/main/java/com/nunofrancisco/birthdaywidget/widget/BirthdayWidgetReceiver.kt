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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // A contagem de dias muda à meia-noite e após arranque/mudança de fuso;
        // nestes casos forçamos uma atualização de todas as instâncias.
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> {
                val pending = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        BirthdayWidget.refreshAll(appContext)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
