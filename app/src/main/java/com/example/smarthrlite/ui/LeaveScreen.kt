package com.example.smarthrlite.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthrlite.model.LeaveRequest
import com.example.smarthrlite.viewmodel.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(vm: LeaveViewModel, role: String, onBack: () -> Unit) {

    val leaves by vm.leaveList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Date Filter State
    var filterDate by remember { mutableStateOf("") }
    var showDatePickerFilter by remember { mutableStateOf(false) }
    val datePickerStateFilter = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (filterDate.isNotEmpty()) {
                        TextButton(onClick = { filterDate = "" }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (role == "Employee") {
                ExtendedFloatingActionButton(
                    onClick = { showDialog = true },
                    icon = { Text("+", fontSize = 24.sp) },
                    text = { Text("Request Leave") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 📅 Date Filter Selector
            OutlinedTextField(
                value = if (filterDate.isEmpty()) "All Dates" else filterDate,
                onValueChange = {},
                label = { Text("Filter by Start Date") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showDatePickerFilter = true },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            val displayedLeaves = if (filterDate.isEmpty()) {
                leaves
            } else {
                leaves.filter { it.startDate == filterDate }
            }

            if (displayedLeaves.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (filterDate.isEmpty()) "No leave requests found" else "No requests for $filterDate",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedLeaves) { leave ->
                        LeaveItem(leave, role) { status ->
                            vm.updateStatus(leave.id, status)
                        }
                    }
                }
            }
        }

        // Apply Leave Dialog
        if (showDialog) {
            ApplyLeaveDialog(
                onDismiss = { showDialog = false },
                onApply = { startDate, endDate, reason ->
                    vm.applyLeave(
                        LeaveRequest(
                            employeeName = "Current User",
                            startDate = startDate,
                            endDate = endDate,
                            reason = reason
                        )
                    )
                    showDialog = false
                }
            )
        }

        // Filter Date Picker
        if (showDatePickerFilter) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerFilter = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerStateFilter.selectedDateMillis?.let {
                            filterDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                        }
                        showDatePickerFilter = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerFilter = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerStateFilter)
            }
        }
    }
}

@Composable
fun LeaveItem(leave: LeaveRequest, role: String, onStatusUpdate: (String) -> Unit) {
    val statusText = leave.statusString
    val statusColor = when (statusText) {
        "Approved" -> Color(0xFF4CAF50)
        "Rejected" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(leave.employeeName, style = MaterialTheme.typography.titleLarge)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("${leave.startDate} to ${leave.endDate}", style = MaterialTheme.typography.bodyMedium)
            Text("Reason: ${leave.reason}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            if (role == "HR" && statusText == "Pending") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStatusUpdate("Approved") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Approve")
                    }
                    Button(
                        onClick = { onStatusUpdate("Rejected") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveDialog(onDismiss: () -> Unit, onApply: (String, String, String) -> Unit) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingForStart by remember { mutableStateOf(true) }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val formattedDate = dateFormatter.format(Date(it))
                        if (pickingForStart) startDate = formattedDate else endDate = formattedDate
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Leave") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { },
                    label = { Text("Start Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickingForStart = true
                            showDatePicker = true
                        },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { },
                    label = { Text("End Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickingForStart = false
                            showDatePicker = true
                        },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(startDate, endDate, reason) },
                enabled = startDate.isNotEmpty() && endDate.isNotEmpty() && reason.isNotEmpty()
            ) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
