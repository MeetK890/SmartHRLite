package com.example.smarthrlite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: String,
    userName: String = "",
    vm: com.example.smarthrlite.viewmodel.EmployeeViewModel,
    onEmployeesClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var totalEmployees by remember { mutableStateOf(0) }
    var todayAttendance by remember { mutableStateOf(0) }
    var pendingLeaves by remember { mutableStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("employees").addSnapshotListener { snapshot, _ ->
            totalEmployees = snapshot?.size() ?: 0
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.collection("attendance")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    todayAttendance = snapshot.documents.count { doc ->
                        val date = doc.get("date")?.toString() ?: ""
                        val status = doc.get("status")?.toString() ?: ""
                        date.startsWith(todayDate) && status == "Present"
                    }
                }
            }

        db.collection("leaves")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, _ ->
                pendingLeaves = snapshot?.size() ?: 0
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SmartHR Lite", 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
                    IconButton(
                        onClick = {
                            auth.signOut()
                            onLogout()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, 
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 👋 Greeting Section
            val displayName = if (userName.isNotEmpty()) userName else role
            Text(
                text = "Hello, $displayName 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Welcome to your HR Dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 📊 Modern Stats Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernDashboardCard(
                    title = "Total Staff",
                    count = totalEmployees,
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ModernDashboardCard(
                    title = "Present",
                    count = todayAttendance,
                    icon = Icons.Default.Info,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ModernDashboardCard(
                    title = "Leaves",
                    count = pendingLeaves,
                    icon = Icons.Default.DateRange,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (role == "HR") {
                ActionCard("Manage Employees", "Add, edit, or remove staff members", onEmployeesClick)
            }

            ActionCard(
                if (role == "HR") "Attendance Records" else "Mark Attendance",
                "Keep track of daily presence and history",
                onAttendanceClick
            )

            ActionCard(
                if (role == "HR") "Leave Approvals" else "Apply for Leave",
                "Manage absence requests efficiently",
                onLeaveClick
            )

            if (role == "HR") {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("System Reset: Clear All Data", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset All Data?") },
                text = { Text("This will permanently delete all employees, attendance logs, and leave requests. This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.clearAllData {
                                showResetDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ModernDashboardCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .height(80.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp, // Simplified arrow for demo
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}
