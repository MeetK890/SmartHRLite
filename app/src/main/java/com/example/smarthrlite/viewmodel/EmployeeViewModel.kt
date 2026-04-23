package com.example.smarthrlite.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthrlite.model.Employee
import com.example.smarthrlite.model.Attendance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EmployeeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _employeeList = MutableStateFlow<List<Employee>>(emptyList())
    val employeeList: StateFlow<List<Employee>> = _employeeList

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    val filteredEmployeeList = _employeeList.combine(_searchQuery) { list, query ->
        if (query.isEmpty()) list
        else list.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.department.contains(query, ignoreCase = true) 
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _attendanceHistory = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceHistory: StateFlow<List<Attendance>> = _attendanceHistory

    val todayAttendanceMap = _attendanceHistory.map { history ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        history.filter { it.date.startsWith(today) }
            .associateBy({ it.employeeId }, { it.statusString })
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    var employees = mutableStateListOf<Employee>()

    init {
        fetch()
        fetchAttendanceHistory()
    }

    private fun checkAuth(onAuthorized: () -> Unit) {
        if (auth.currentUser != null) {
            onAuthorized()
        } else {
            viewModelScope.launch {
                _errorMessage.emit("User not authenticated")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun fetch() {
        checkAuth {
            db.collection("employees").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.toObjects(Employee::class.java)
                    list.forEachIndexed { index, employee ->
                        employee.id = snapshot.documents[index].id
                    }
                    _employeeList.value = list
                    employees.clear()
                    employees.addAll(list)
                }
            }
        }
    }

    fun fetchAttendanceHistory() {
        checkAuth {
            db.collection("attendance")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Attendance::class.java)
                        _attendanceHistory.value = list
                    }
                }
        }
    }

    fun addEmployee(emp: Employee) {
        checkAuth {
            db.collection("employees").add(emp).addOnSuccessListener {
                fetch()
            }
        }
    }

    fun deleteEmployee(id: String) {
        checkAuth {
            db.collection("employees").document(id).delete().addOnSuccessListener {
                fetch()
            }
        }
    }

    fun updateEmployee(id: String, emp: Employee) {
        checkAuth {
            db.collection("employees").document(id).set(emp).addOnSuccessListener {
                fetch()
            }
        }
    }

    fun clearAllData(onSuccess: () -> Unit = {}) {
        checkAuth {
            viewModelScope.launch {
                val collections = listOf("employees", "attendance", "leaves")
                var completed = 0
                collections.forEach { collectionName ->
                    db.collection(collectionName).get().addOnSuccessListener { snapshot ->
                        val batch = db.batch()
                        snapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        batch.commit().addOnSuccessListener {
                            completed++
                            if (completed == collections.size) {
                                onSuccess()
                            }
                        }
                    }
                }
            }
        }
    }

    fun markAttendance(empId: String, empName: String, status: String, onSuccess: () -> Unit = {}) {
        checkAuth {
            // Check if already marked today
            val alreadyMarked = todayAttendanceMap.value.containsKey(empId)
            
            if (alreadyMarked) {
                viewModelScope.launch {
                    _errorMessage.emit("Attendance already marked for $empName today.")
                }
                return@checkAuth
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(Date())
            val attendance = Attendance(
                employeeId = empId,
                employeeName = empName,
                status = status,
                date = dateString
            )
            db.collection("attendance").add(attendance).addOnSuccessListener {
                onSuccess()
            }
        }
    }
}
