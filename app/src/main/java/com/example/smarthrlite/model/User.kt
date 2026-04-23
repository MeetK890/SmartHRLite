package com.example.smarthrlite.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Employee" // HR or Employee
)
