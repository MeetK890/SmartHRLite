package com.example.smarthrlite.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smarthrlite.model.User

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun register(name: String, email: String, password: String, role: String, onSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = User(
                    id = result.user!!.uid,
                    name = name,
                    email = email,
                    role = role
                )
                db.collection("users").document(user.id).set(user)
                onSuccess()
            }
    }

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                db.collection("users").document(uid).get().addOnSuccessListener {
                    val role = it.getString("role") ?: "Employee"
                    onSuccess(role)
                }
            }
    }
}
