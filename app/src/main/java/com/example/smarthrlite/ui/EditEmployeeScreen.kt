package com.example.smarthrlite.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmployeeScreen(
    employeeId: String,
    onUpdateSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onBack: () -> Unit
) {

    val db = FirebaseFirestore.getInstance()

    // ✅ EMPTY INITIAL STATE (IMPORTANT)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    // 🔥 FETCH DATA FROM FIREBASE
    LaunchedEffect(employeeId) {
        db.collection("employees")
            .document(employeeId)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    name = document.getString("name") ?: ""
                    email = document.getString("email") ?: ""
                    phone = document.getString("phone") ?: ""
                    position = document.getString("position") ?: ""
                    department = document.getString("department") ?: ""
                }
            }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Employee") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
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

            // 🔄 UPDATE
            Button(
                onClick = {

                    val updatedEmployee = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "position" to position,
                        "department" to department
                    )

                    db.collection("employees")
                        .document(employeeId)   // ✅ FIXED
                        .set(updatedEmployee)
                        .addOnSuccessListener {
                            onUpdateSuccess()
                        }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ❌ DELETE
            Button(
                onClick = {

                    db.collection("employees")
                        .document(employeeId)   // ✅ FIXED
                        .delete()
                        .addOnSuccessListener {
                            onDeleteSuccess()
                        }

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    }
}
