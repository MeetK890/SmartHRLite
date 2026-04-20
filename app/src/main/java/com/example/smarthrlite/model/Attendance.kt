package com.example.smarthrlite.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Exclude

@IgnoreExtraProperties
data class Attendance(
    var id: String = "",
    
    @get:PropertyName("employeeId") @set:PropertyName("employeeId")
    var employeeId: String = "",
    
    @get:PropertyName("employeeName") @set:PropertyName("employeeName")
    var employeeName: String = "",
    
    @get:PropertyName("date") @set:PropertyName("date")
    var date: String = "",
    
    @get:PropertyName("status") @set:PropertyName("status")
    var status: Any? = null
) {
    @get:Exclude
    val statusString: String
        get() = when (status) {
            is String -> status as String
            is List<*> -> (status as List<*>).firstOrNull()?.toString() ?: ""
            else -> status?.toString() ?: ""
        }

    // Support for legacy field names found in Firestore
    @get:PropertyName("empId") @set:PropertyName("empId")
    var legacyEmpId: String? = null
        set(value) {
            if (employeeId.isEmpty() && value != null) {
                this.employeeId = value
            }
            field = value
        }

    @get:PropertyName("employee_id") @set:PropertyName("employee_id")
    var legacyEmployeeId: String? = null
        set(value) {
            if (employeeId.isEmpty() && value != null) {
                this.employeeId = value
            }
            field = value
        }
}
