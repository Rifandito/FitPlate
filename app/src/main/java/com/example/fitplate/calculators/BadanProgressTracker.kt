package com.example.fitplate.calculators

import android.util.Log
import com.example.fitplate.RealtimeDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BadanProgressTracker {

    private val dbUser = RealtimeDatabase.instance().getReference("users")

    interface ProgressCallback {
        fun onSuccess(data: Map<String, Double>)
        fun onFailure(errorMessage: String)
    }

    interface UpdateCallback {
        fun onSuccess(message: String)
        fun onFailure(errorMessage: String)
    }

    fun fetchUserData(userId: String, callback: ProgressCallback) {
        dbUser.child(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val beratBadan = snapshot.child("beratBadan").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val targetBb = snapshot.child("targetBb").value?.toString()?.toDoubleOrNull() ?: 0.0
                    Log.d("FirebaseDebug", "beratBadan: $beratBadan, targetBb: $targetBb")

                    val progress = mapOf(
                        "beratBadan" to beratBadan,
                        "targetBb" to targetBb,
                        "tinggiBadan" to (snapshot.child("tinggiBadan").value?.toString()?.toDoubleOrNull() ?: 0.0)
                    )
                    callback.onSuccess(progress)
                } else {
                    callback.onFailure("No data found for this user")
                }
            }


            override fun onCancelled(error: DatabaseError) {
                callback.onFailure("Failed to fetch progress: ${error.message}")
            }
        })
    }

    // Update user data function using input map
    fun updateUserData(userId: String, updatedData: Map<String, Double>, callback: UpdateCallback) {
        if (updatedData.isNotEmpty()) {
            dbUser.child(userId).updateChildren(updatedData.mapValues { it.value.toString() }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseDebug", "User data updated successfully: $updatedData")
                    callback.onSuccess("Data updated successfully")
                } else {
                    Log.e("FirebaseDebug", "Failed to update user data: ${task.exception?.message}")
                    callback.onFailure("Failed to update data: ${task.exception?.message}")
                }
            }
        } else {
            callback.onFailure("No valid data to update")
        }
    }
}
