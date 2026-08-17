package com.nunofrancisco.birthdaywidget.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nunofrancisco.birthdaywidget.MainActivity
import com.nunofrancisco.birthdaywidget.R
import com.nunofrancisco.birthdaywidget.data.BirthdayRepository
import com.nunofrancisco.birthdaywidget.util.BirthdayCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BirthdayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Popula o estado deste widget a partir da base de dados (primeira
        // colocação e atualizações do sistema). O conteúdo é depois desenhado
        // a partir do estado, que a Glance observa e recompõe automaticamente.
        updateAppWidgetState(context, id) { prefs ->
            prefs.applyNext(computeNext(context))
        }

        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val hasNext = prefs[HAS_NEXT_KEY] ?: false
        val name = prefs[NAME_KEY]
        val date = prefs[DATE_KEY]
        val countdown = prefs[COUNTDOWN_KEY]

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(16.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(
                    actionStartActivity(
                        Intent(LocalContext.current, MainActivity::class.java)
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_cake),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp),
            )
            Spacer(GlanceModifier.width(8.dp))

            if (!hasNext || name == null) {
                Text(
                    text = "Sem aniversários",
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 14.sp,
                    ),
                )
            } else {
                Text(
                    text = name + " · " + date.orEmpty(),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = countdown.orEmpty(),
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }

    companion object {
        private val HAS_NEXT_KEY = booleanPreferencesKey("has_next")
        private val NAME_KEY = stringPreferencesKey("name")
        private val DATE_KEY = stringPreferencesKey("date")
        private val COUNTDOWN_KEY = stringPreferencesKey("countdown")

        private val ptLocale = Locale("pt", "PT")
        private val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", ptLocale)
        private val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM", ptLocale)

        fun formatDate(date: LocalDate): String = date.format(dateFormatter)

        fun formatDateShort(date: LocalDate): String = date.format(shortDateFormatter)

        fun countdownLabel(days: Long): String = when (days) {
            0L -> "É hoje! 🎉"
            1L -> "É amanhã"
            else -> "faltam $days dias"
        }

        private suspend fun computeNext(context: Context): NextInfo? {
            val birthdays = BirthdayRepository.getInstance(context).getAll()
            val next = BirthdayCalculator.next(birthdays, LocalDate.now()) ?: return null
            return NextInfo(
                name = next.birthday.name,
                date = formatDateShort(next.nextDate),
                countdown = countdownLabel(next.daysUntil),
            )
        }

        private fun MutablePreferences.applyNext(next: NextInfo?) {
            if (next == null) {
                this[HAS_NEXT_KEY] = false
                remove(NAME_KEY)
                remove(DATE_KEY)
                remove(COUNTDOWN_KEY)
            } else {
                this[HAS_NEXT_KEY] = true
                this[NAME_KEY] = next.name
                this[DATE_KEY] = next.date
                this[COUNTDOWN_KEY] = next.countdown
            }
        }

        /**
         * Recalcula o próximo aniversário e escreve-o no estado de todas as
         * instâncias do widget. A Glance recompõe reativamente ao ver o estado
         * mudar; o updateAll cobre também instâncias sem composição ativa.
         */
        suspend fun refreshAll(context: Context) {
            val next = computeNext(context)
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(BirthdayWidget::class.java)
            ids.forEach { id ->
                updateAppWidgetState(context, id) { prefs -> prefs.applyNext(next) }
            }
            BirthdayWidget().updateAll(context)
        }
    }

    private data class NextInfo(
        val name: String,
        val date: String,
        val countdown: String,
    )
}
