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

    /**
     * Creates a Firestore user document only if one does not already exist.
     * Safe to call on every login — existing users are never overwritten.
     */
    fun ensureUserExists(uid: String, name: String, email: String, onResult: (Boolean) -> Unit = {}) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(true)
                } else {
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email,
                        createdAt = System.currentTimeMillis()
                    )
                    saveUser(user, onResult)
                }
            }
            .addOnFailureListener { onResult(false) }
    }
}
