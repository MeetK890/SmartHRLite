package com.example.smarthrlite.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var totalEmployees = mutableStateOf(0)
    var todayAttendance = mutableStateOf(0)
    var pendingLeaves = mutableStateOf(0)

    init {
        loadData()
    }

    fun loadData() {
        db.collection("employees").get().addOnSuccessListener {
            totalEmployees.value = it.size()
        }

        db.collection("attendance").get().addOnSuccessListener {
            todayAttendance.value = it.size()
        }

        db.collection("leaves").whereEqualTo("status", "Pending").get().addOnSuccessListener {
            pendingLeaves.value = it.size()
        }
    }
}
