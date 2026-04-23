package com.example.smarthrlite.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Exclude

@IgnoreExtraProperties
data class LeaveRequest(
    var id: String = "",
    
    @get:PropertyName("employeeId") @set:PropertyName("employeeId")
    var employeeId: String = "",
    
    @get:PropertyName("employeeName") @set:PropertyName("employeeName")
    var employeeName: String = "",
    
    @get:PropertyName("startDate") @set:PropertyName("startDate")
    var startDate: String = "",
    
    @get:PropertyName("endDate") @set:PropertyName("endDate")
    var endDate: String = "",
    
    @get:PropertyName("reason") @set:PropertyName("reason")
    var reason: String = "",
    
    @get:PropertyName("status") @set:PropertyName("status")
    var status: Any? = "Pending"
) {
    @get:Exclude
    val statusString: String
        get() = when (status) {
            is String -> status as String
            else -> status?.toString() ?: "Pending"
        }

    // Support for legacy snake_case field names found in Firestore
    @get:PropertyName("employee_id") @set:PropertyName("employee_id")
    var legacyEmployeeId: String? = null
        set(value) {
            if (employeeId.isEmpty() && value != null) {
                this.employeeId = value
            }
            field = value
        }

    @get:PropertyName("employee_name") @set:PropertyName("employee_name")
    var legacyEmployeeName: String? = null
        set(value) {
            if (employeeName.isEmpty() && value != null) {
                this.employeeName = value
            }
            field = value
        }

    @get:PropertyName("start_date") @set:PropertyName("start_date")
    var legacyStartDate: String? = null
        set(value) {
            if (startDate.isEmpty() && value != null) {
                this.startDate = value
            }
            field = value
        }

    @get:PropertyName("end_date") @set:PropertyName("end_date")
    var legacyEndDate: String? = null
        set(value) {
            if (endDate.isEmpty() && value != null) {
                this.endDate = value
            }
            field = value
        }
}
