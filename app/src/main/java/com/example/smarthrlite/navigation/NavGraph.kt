package com.example.smarthrlite.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthrlite.ui.*
import com.example.smarthrlite.viewmodel.EmployeeViewModel
import com.example.smarthrlite.viewmodel.LeaveViewModel

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    val employeeViewModel: EmployeeViewModel = viewModel()
    val leaveViewModel: LeaveViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // 🔐 LOGIN
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = { result ->
                    navController.navigate("dashboard/$result") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 📝 REGISTER
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // 🏠 DASHBOARD
        composable("dashboard/{data}") { backStackEntry ->
            val data = backStackEntry.arguments?.getString("data") ?: "Employee|"
            val role = data.substringBefore("|")
            val name = data.substringAfter("|")

            DashboardScreen(
                role = role,
                userName = name,
                vm = employeeViewModel,
                onEmployeesClick = {
                    navController.navigate("employees")
                },
                onAttendanceClick = {
                    navController.navigate("attendance")
                },
                onLeaveClick = {
                    navController.navigate("leave/$role")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 👨‍💼 EMPLOYEE LIST
        composable("employees") {
            EmployeeListScreen(
                vm = employeeViewModel,
                onAddClick = {
                    navController.navigate("addEmployee")
                },
                onItemClick = { employee ->
                    navController.navigate("editEmployee/${employee.id}")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ➕ ADD EMPLOYEE
        composable("addEmployee") {
            AddEmployeeScreen(
                viewModel = employeeViewModel,
                employeeToEdit = null,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ✏️ EDIT EMPLOYEE
        composable("editEmployee/{employeeId}") { backStackEntry ->

            val employeeId =
                backStackEntry.arguments?.getString("employeeId") ?: ""

            EditEmployeeScreen(
                employeeId = employeeId,
                onUpdateSuccess = {
                    navController.popBackStack()
                },
                onDeleteSuccess = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 📅 ATTENDANCE
        composable("attendance") {
            AttendanceScreen(
                vm = employeeViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 📄 LEAVE
        composable("leave/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "Employee"

            LeaveScreen(
                vm = leaveViewModel,
                role = role,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
