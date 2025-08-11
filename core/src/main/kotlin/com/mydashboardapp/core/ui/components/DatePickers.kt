package com.mydashboardapp.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Date",
    placeholder: String = "Choose a date",
    enabled: Boolean = true,
    isRequired: Boolean = false,
    supportingText: String? = null,
    isError: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
    )
    
    val formattedDate = selectedDate?.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    ) ?: ""
    
    OutlinedTextField(
        value = formattedDate,
        onValueChange = { /* Read-only */ },
        label = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label)
                if (isRequired) {
                    Text(
                        text = " *",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar",
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        },
        trailingIcon = if (selectedDate != null && enabled) {
            {
                IconButton(onClick = { onDateSelected(null) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        readOnly = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDatePicker = true }
            .semantics {
                contentDescription = if (selectedDate != null) {
                    "$label: ${formattedDate} selected, tap to change"
                } else {
                    "$label: $placeholder, tap to select"
                }
            }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                onDateSelected(
                    dateMillis?.let {
                        LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    }
                )
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            state = datePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Time",
    placeholder: String = "Choose a time",
    enabled: Boolean = true,
    isRequired: Boolean = false,
    supportingText: String? = null,
    isError: Boolean = false,
    is24Hour: Boolean = false
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 12,
        initialMinute = selectedTime?.minute ?: 0,
        is24Hour = is24Hour
    )
    
    val formattedTime = selectedTime?.format(
        if (is24Hour) DateTimeFormatter.ofPattern("HH:mm")
        else DateTimeFormatter.ofPattern("h:mm a")
    ) ?: ""
    
    OutlinedTextField(
        value = formattedTime,
        onValueChange = { /* Read-only */ },
        label = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label)
                if (isRequired) {
                    Text(
                        text = " *",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Clock",
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        },
        trailingIcon = if (selectedTime != null && enabled) {
            {
                IconButton(onClick = { onTimeSelected(null) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear time",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        readOnly = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showTimePicker = true }
            .semantics {
                contentDescription = if (selectedTime != null) {
                    "$label: ${formattedTime} selected, tap to change"
                } else {
                    "$label: $placeholder, tap to select"
                }
            }
    )
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                onTimeSelected(LocalTime.of(hour, minute))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            state = timePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    onDateSelected: (LocalDate?) -> Unit,
    onTimeSelected: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    dateLabel: String = "Date",
    timeLabel: String = "Time",
    enabled: Boolean = true,
    isRequired: Boolean = false,
    is24Hour: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (dateLabel.isNotEmpty() || timeLabel.isNotEmpty()) {
            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                label = dateLabel,
                enabled = enabled,
                isRequired = isRequired,
                modifier = Modifier.weight(1f)
            )
            
            TimePickerField(
                selectedTime = selectedTime,
                onTimeSelected = onTimeSelected,
                label = timeLabel,
                enabled = enabled,
                isRequired = isRequired,
                is24Hour = is24Hour,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    state: DatePickerState
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                DatePicker(
                    state = state,
                    modifier = Modifier.semantics {
                        contentDescription = "Date picker calendar"
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    TextButton(
                        onClick = { 
                            onDateSelected(state.selectedDateMillis)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    state: TimePickerState
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TimePicker(
                    state = state,
                    modifier = Modifier.semantics {
                        contentDescription = "Time picker"
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    TextButton(
                        onClick = { 
                            onTimeSelected(state.hour, state.minute)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun DateRangePicker(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate?) -> Unit,
    onEndDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Date Range",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(start = 16.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DatePickerField(
                selectedDate = startDate,
                onDateSelected = onStartDateSelected,
                label = "Start Date",
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            
            DatePickerField(
                selectedDate = endDate,
                onDateSelected = onEndDateSelected,
                label = "End Date",
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Validation message
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            Text(
                text = "Start date must be before end date",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DatePickersPreview() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(LocalTime.now()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Date Picker", style = MaterialTheme.typography.headlineSmall)
        DatePickerField(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            label = "Event Date",
            isRequired = true
        )
        
        Text("Time Picker", style = MaterialTheme.typography.headlineSmall)
        TimePickerField(
            selectedTime = selectedTime,
            onTimeSelected = { selectedTime = it },
            label = "Event Time",
            is24Hour = false
        )
        
        Text("Date & Time Picker", style = MaterialTheme.typography.headlineSmall)
        DateTimePickerField(
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            onDateSelected = { selectedDate = it },
            onTimeSelected = { selectedTime = it }
        )
        
        Text("Date Range Picker", style = MaterialTheme.typography.headlineSmall)
        DateRangePicker(
            startDate = startDate,
            endDate = endDate,
            onStartDateSelected = { startDate = it },
            onEndDateSelected = { endDate = it }
        )
    }
}
