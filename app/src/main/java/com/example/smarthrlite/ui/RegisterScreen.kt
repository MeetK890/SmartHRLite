package com.example.smarthrlite.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.smarthrlite.util.ValidationUtils
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Employee") }

    val roles = listOf("Employee", "HR")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Create Account", style = MaterialTheme.typography.headlineLarge)
        Text("Join SmartHR Lite today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Role", style = MaterialTheme.typography.titleMedium)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            roles.forEach { text ->
                Row(
                    Modifier
                        .height(48.dp)
                        .selectable(
                            selected = (text == role),
                            onClick = { role = text },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == role),
                        onClick = null // null recommended for accessibility with selectable modifier
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val emailTrimmed = email.trim()
                if (!ValidationUtils.isNotBlank(name)) {
                    Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!ValidationUtils.isValidEmail(emailTrimmed)) {
                    Toast.makeText(context, "Enter a valid email", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!ValidationUtils.isValidPassword(password)) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                auth.createUserWithEmailAndPassword(emailTrimmed, password)
                    .addOnSuccessListener { result ->
                        val userId = result.user?.uid

                        val user = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "role" to role
                        )

                        userId?.let {
                            db.collection("users").document(it).set(user)
                        }

                        Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Register")
        }
    }
}
