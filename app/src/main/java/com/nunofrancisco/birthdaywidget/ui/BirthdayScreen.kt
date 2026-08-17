package com.nunofrancisco.birthdaywidget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import com.nunofrancisco.birthdaywidget.util.UpcomingBirthday
import com.nunofrancisco.birthdaywidget.widget.BirthdayWidget
import java.time.LocalDate
import java.time.YearMonth

private val MONTHS_PT = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(viewModel: BirthdayViewModel = viewModel()) {
    val upcoming by viewModel.upcoming.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Aniversários") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar aniversário")
            }
        },
    ) { padding ->
        if (upcoming.isEmpty()) {
            EmptyState(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(upcoming, key = { it.birthday.id }) { item ->
                    BirthdayCard(item, onDelete = { viewModel.deleteBirthday(item.birthday) })
                }
            }
        }
    }

    if (showDialog) {
        AddBirthdayDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, day, month, year ->
                viewModel.addBirthday(name, day, month, year)
                showDialog = false
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Cake,
                contentDescription = null,
                modifier = Modifier.width(64.dp).height(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text("Ainda não há aniversários", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Toque em + para adicionar o primeiro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BirthdayCard(item: UpcomingBirthday, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.birthday.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                val ageSuffix = item.turningAge?.let { " · faz $it anos" } ?: ""
                Text(
                    BirthdayWidget.formatDate(item.nextDate) + ageSuffix,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    BirthdayWidget.countdownLabel(item.daysUntil),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Apagar",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBirthdayDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, day: Int, month: Int, year: Int?) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var monthIndex by rememberSaveable { mutableStateOf(0) }
    var dayText by rememberSaveable { mutableStateOf("") }
    var yearText by rememberSaveable { mutableStateOf("") }
    var monthExpanded by remember { mutableStateOf(false) }

    val day = dayText.toIntOrNull()
    val year = yearText.toIntOrNull()
    val currentYear = LocalDate.now().year
    val maxDay = daysInMonth(monthIndex + 1, year)

    val nameValid = name.isNotBlank()
    val dayValid = day != null && day in 1..maxDay
    val yearValid = yearText.isBlank() || (year != null && year in 1900..currentYear)
    val formValid = nameValid && dayValid && yearValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo aniversário") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = it },
                ) {
                    OutlinedTextField(
                        value = MONTHS_PT[monthIndex],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mês") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false },
                    ) {
                        MONTHS_PT.forEachIndexed { index, monthName ->
                            DropdownMenuItem(
                                text = { Text(monthName) },
                                onClick = {
                                    monthIndex = index
                                    monthExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                Row {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { input ->
                            dayText = input.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Dia") },
                        isError = dayText.isNotEmpty() && !dayValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { input ->
                            yearText = input.filter { it.isDigit() }.take(4)
                        },
                        label = { Text("Ano (opcional)") },
                        isError = !yearValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = formValid,
                onClick = { onConfirm(name, day!!, monthIndex + 1, year) },
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

private fun daysInMonth(month: Int, year: Int?): Int {
    return if (year != null) {
        YearMonth.of(year, month).lengthOfMonth()
    } else {
        // Sem ano assumimos ano bissexto para permitir 29 de Fevereiro.
        YearMonth.of(2000, month).lengthOfMonth()
    }
}
