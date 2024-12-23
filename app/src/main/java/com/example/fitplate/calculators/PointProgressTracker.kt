package com.example.fitplate.calculators

import android.util.Log
import com.example.fitplate.RealtimeDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PointProgressTracker {

    private val dbPoint = RealtimeDatabase.instance().getReference("Skor")
    private val dbMedaliUser = RealtimeDatabase.instance().getReference("UserMedalSum")

    interface ProgressCallback {
        fun onSuccess(data: Map<String, Any?>)
        fun onFailure(errorMessage: String)
    }

    fun fetchSkorData(userId: String, callback: ProgressCallback) {
        dbPoint.child(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val skor = snapshot.value?.toString()?.toIntOrNull() ?: 0
                    callback.onSuccess(mapOf("skor" to skor))
                } else {
                    callback.onFailure("No score data found for this user")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback.onFailure("Failed to fetch progress: ${error.message}")
            }
        })
    }

    fun fetchUserMedalData(userId: String, callback: ProgressCallback) {
        dbMedaliUser.child(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val goldCount = snapshot.child("goldCount").value?.toString()?.toIntOrNull() ?: 0
                    val silverCount = snapshot.child("silverCount").value?.toString()?.toIntOrNull() ?: 0
                    val bronzeCount = snapshot.child("bronzeCount").value?.toString()?.toIntOrNull() ?: 0

                    callback.onSuccess(
                        mapOf(
                            "goldCount" to goldCount,
                            "silverCount" to silverCount,
                            "bronzeCount" to bronzeCount
                        )
                    )
                } else {
                    callback.onFailure("No medal data found for this user")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback.onFailure("Failed to fetch progress: ${error.message}")
            }
        })
    }
}
