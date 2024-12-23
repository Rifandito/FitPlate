package com.example.fitplate.calculators

import com.example.fitplate.RealtimeDatabase

class PointCalculator {

    private val dbPoint = RealtimeDatabase.instance().getReference("Skor")
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
                // Create a mutable map to work with
                val userMedal = mutableMapOf(
                    "goldCount" to (snapshot.child("goldCount").value?.toString()?.toDoubleOrNull() ?: 0.0),
                    "silverCount" to (snapshot.child("silverCount").value?.toString()?.toDoubleOrNull() ?: 0.0),
                    "bronzeCount" to (snapshot.child("bronzeCount").value?.toString()?.toDoubleOrNull() ?: 0.0)
                )
                // Update the counts based on awarded medals
                medals.forEach { (medalType, points) ->
                    when (medalType) {
                        "emas" -> userMedal["goldCount"] = userMedal["goldCount"]!! + 1
                        "perak" -> userMedal["silverCount"] = userMedal["silverCount"]!! + 1
                        "perunggu" -> userMedal["bronzeCount"] = userMedal["bronzeCount"]!! + 1
                    }
                    updatePoints(userId, points) // Add points for each medal
                }

                // Save the updated map back to the database
                dbMedaliUser.child(userId).setValue(userMedal).addOnSuccessListener {
                    onSuccess("All medals awarded and points updated!")
                }.addOnFailureListener {
                    onFailure("Failed to update medals.")
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