package com.nunofrancisco.birthdaywidget.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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
import com.nunofrancisco.birthdaywidget.util.UpcomingBirthday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BirthdayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val birthdays = BirthdayRepository.getInstance(context).getAll()
        val next = BirthdayCalculator.next(birthdays, LocalDate.now())

        provideContent {
            GlanceTheme {
                WidgetContent(next)
            }
        }
    }

    @Composable
    private fun WidgetContent(next: UpcomingBirthday?) {
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

            if (next == null) {
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
                    text = next.birthday.name + " · " + formatDate(next.nextDate),
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
                    text = countdownLabel(next.daysUntil),
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
        private val ptLocale = Locale("pt", "PT")
        private val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", ptLocale)

        fun formatDate(date: LocalDate): String = date.format(dateFormatter)

        fun countdownLabel(days: Long): String = when (days) {
            0L -> "É hoje! 🎉"
            1L -> "É amanhã"
            else -> "faltam $days dias"
        }
    }
}
