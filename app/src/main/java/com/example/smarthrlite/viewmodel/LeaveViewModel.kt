package com.example.smarthrlite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.smarthrlite.model.LeaveRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LeaveViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _leaveList = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveList: StateFlow<List<LeaveRequest>> = _leaveList

    init {
        fetchLeaves()
    }

    fun applyLeave(leave: LeaveRequest) {
        db.collection("leaves").add(leave)
    }

    fun fetchLeaves() {
        db.collection("leaves").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.toObjects(LeaveRequest::class.java)
                list.forEachIndexed { index, leave ->
                    leave.id = snapshot.documents[index].id
                }
                _leaveList.value = list
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        db.collection("leaves").document(id).update("status", status)
        fetchLeaves()
    }
}
