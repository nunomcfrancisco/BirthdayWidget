package com.nunofrancisco.birthdaywidget.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Atualiza o widget uma vez por dia, à meia-noite, e reagenda-se a si próprio.
 * É isto que faz a contagem de "dias em falta" diminuir de dia para dia, mesmo
 * que a app não seja aberta.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        BirthdayWidget.refreshAll(applicationContext)
        schedule(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "birthday_widget_daily_refresh"

        /** Agenda a próxima atualização para pouco depois da próxima meia-noite. */
        fun schedule(context: Context) {
            val now = ZonedDateTime.now()
            val nextMidnight = now.toLocalDate()
                .plusDays(1)
                .atTime(LocalTime.of(0, 1))
                .atZone(now.zone)
            val delayMillis = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)

            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        }
    }
}
