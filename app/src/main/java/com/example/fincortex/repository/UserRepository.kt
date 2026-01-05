package com.example.fincortex.repository

import com.example.fincortex.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        usersCollection
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun getUser(uid: String, onResult: (User?) -> Unit) {
        usersCollection
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(document.toObject(User::class.java))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
