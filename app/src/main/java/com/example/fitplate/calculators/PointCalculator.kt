package com.example.fitplate.calculators

import com.example.fitplate.RealtimeDatabase
import com.example.fitplate.dataclasses.UserMedalSum

class PointCalculator {

    private val dbPoint = RealtimeDatabase.instance().getReference("Skor")
    private val dbMedali = RealtimeDatabase.instance().getReference("medali")
    private val dbMedaliUser = RealtimeDatabase.instance().getReference("UserMedalSum")

    fun calculateAndAwardMedals(
        userId: String,
        progress: Double,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val medalsToAward = mutableListOf<Pair<String, Int>>()

        // Determine medals to award based on progress
        if (progress >= 50) medalsToAward.add("perunggu" to 10)
        if (progress >= 80) medalsToAward.add("perak" to 15)
        if (progress >= 100) medalsToAward.add("emas" to 20)

        if (medalsToAward.isNotEmpty()) {
            awardMedals(userId, medalsToAward, onSuccess, onFailure)
        } else {
            onFailure("No medals to award. Progress is below 50%.")
        }
    }

    private fun awardMedals(
        userId: String,
        medals: List<Pair<String, Int>>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        dbMedaliUser.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userMedal = snapshot.getValue(UserMedalSum::class.java)
                userMedal?.let {
                    medals.forEach { (medalType, points) ->
                        when (medalType) {
                            "emas" -> it.goldCount++
                            "perak" -> it.silverCount++
                            "perunggu" -> it.bronzeCount++
                        }
                        updatePoints(userId, points) // Add points for each medal
                    }

                    // Save updated medal data
                    dbMedaliUser.child(userId).setValue(it).addOnSuccessListener {
                        onSuccess("All medals awarded and points updated!")
                    }.addOnFailureListener {
                        onFailure("Failed to update medals.")
                    }
                }
            } else {
                onFailure("User medal data not found.")
            }
        }.addOnFailureListener {
            onFailure("Failed to fetch user medal data.")
        }
    }

    private fun updatePoints(userId: String, points: Int) {
        dbPoint.child(userId).get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toIntOrNull() ?: 0
            dbPoint.child(userId).setValue(currentPoints + points)
        }
    }
}