package com.example.smarthrlite.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthrlite.model.Employee
import com.example.smarthrlite.viewmodel.EmployeeViewModel
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeScreen(
    viewModel: EmployeeViewModel,
    employeeToEdit: Employee? = null,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(employeeToEdit?.name ?: "") }
    var email by remember { mutableStateOf(employeeToEdit?.email ?: "") }
    var phone by remember { mutableStateOf(employeeToEdit?.phone ?: "") }
    var position by remember { mutableStateOf(employeeToEdit?.position ?: "") }
    var department by remember { mutableStateOf(employeeToEdit?.department ?: "") }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (employeeToEdit == null) "Add Employee" else "Edit Employee") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = { Text("Position") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val employee = Employee(
                        id = employeeToEdit?.id ?: "",
                        name = name,
                        email = email,
                        phone = phone,
                        position = position,
                        department = department
                    )
                    
                    if (employeeToEdit == null) {
                        viewModel.addEmployee(employee)
                    } else {
                        viewModel.updateEmployee(employee.id, employee)
                    }
                    onSaveSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (employeeToEdit == null) "Save Employee" else "Update Employee")
            }
        }
    }
}
