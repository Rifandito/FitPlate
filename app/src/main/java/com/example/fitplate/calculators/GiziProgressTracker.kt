package com.example.fitplate.calculators

import android.widget.Toast
import com.example.fitplate.RealtimeDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.*
import java.util.*

class GiziProgressTracker {

    private val dbMakanan = RealtimeDatabase.instance().getReference("DataMakananUser")
    private val dbProgresGizi = RealtimeDatabase.instance().getReference("ProgressGiziHarian")
    private val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    interface ProgressCallback {
        fun onSuccess(data: Map<String, Double>)
        fun onFailure(errorMessage: String)
    }

    fun fetchDailyProgress(userId: String, callback: ProgressCallback) {
        dbProgresGizi.child(userId).child(dateKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val progress = mapOf(
                        "jumlahKalori" to (snapshot.child("jumlahKalori").value?.toString()?.toDoubleOrNull() ?: 0.0),
                        "jumlahKarbohidrat" to (snapshot.child("jumlahKarbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0),
                        "jumlahProtein" to (snapshot.child("jumlahProtein").value?.toString()?.toDoubleOrNull() ?: 0.0),
                        "jumlahLemak" to (snapshot.child("jumlahLemak").value?.toString()?.toDoubleOrNull() ?: 0.0)
                    )
                    callback.onSuccess(progress)
                } else {
                    callback.onFailure("Kamu belum ada progres buat hari ini")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure("Failed to fetch progress: ${error.message}")
            }
        })
    }

    fun addDailyProgress(userId: String, callback: (Boolean, String) -> Unit) {
        dbMakanan.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var totalCalorie = 0.0
                var totalProtein = 0.0
                var totalKarbo = 0.0
                var totalLemak = 0.0

                fun calculateProgress(newValue: Double?, existingValue: Double?): Double {
                    return (existingValue ?: 0.0) + (newValue ?: 0.0)
                }

                for (foodSnapshot in snapshot.children) {
                    val banyakCalorie = foodSnapshot.child("kalori").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val banyakProtein = foodSnapshot.child("protein").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val banyakKarbo = foodSnapshot.child("karbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val banyakLemak = foodSnapshot.child("lemak").value?.toString()?.toDoubleOrNull() ?: 0.0

                    // Accumulate the values
                    totalCalorie = calculateProgress(banyakCalorie, totalCalorie)
                    totalProtein = calculateProgress(banyakProtein, totalProtein)
                    totalKarbo = calculateProgress(banyakKarbo, totalKarbo)
                    totalLemak = calculateProgress(banyakLemak, totalLemak)
                }

                val updatedProgress = mapOf(
                    "jumlahKalori" to totalCalorie,
                    "jumlahKarbohidrat" to totalKarbo,
                    "jumlahProtein" to totalProtein,
                    "jumlahLemak" to totalLemak,
                    "status" to "progress saved"
                )

                dbProgresGizi.child(userId).child(dateKey).updateChildren(updatedProgress)
                    .addOnSuccessListener { callback(true, "Progres kamu bertambah!") }
                    .addOnFailureListener { callback(false, "Gagal memperbarui progress gizi: ${it.message}") }
            } else {
                callback(false, "Kamu belum ada progres buat hari ini")
            }
        }.addOnFailureListener { callback(false, "Failed to fetch food data: ${it.message}") }
    }

    fun reduceDailyProgress(userId: String, foodId: String, callback: (Boolean, String) -> Unit) {
        dbMakanan.child(userId).child(dateKey).child(foodId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val calories = snapshot.child("kalori").value?.toString()?.toDoubleOrNull() ?: 0.0
                val protein = snapshot.child("protein").value?.toString()?.toDoubleOrNull() ?: 0.0
                val carbs = snapshot.child("karbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
                val fat = snapshot.child("lemak").value?.toString()?.toDoubleOrNull() ?: 0.0

                // Remove the food item
                dbMakanan.child(userId).child(dateKey).child(foodId).removeValue().addOnSuccessListener {
                    dbProgresGizi.child(userId).child(dateKey).get().addOnSuccessListener { progressSnapshot ->
                        if (progressSnapshot.exists()) {
                            val currentCalories = progressSnapshot.child("jumlahKalori").value?.toString()?.toDoubleOrNull() ?: 0.0
                            val currentProtein = progressSnapshot.child("jumlahProtein").value?.toString()?.toDoubleOrNull() ?: 0.0
                            val currentCarbs = progressSnapshot.child("jumlahKarbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
                            val currentFat = progressSnapshot.child("jumlahLemak").value?.toString()?.toDoubleOrNull() ?: 0.0

                            val updatedProgress = mapOf(
                                "jumlahKalori" to (currentCalories - calories).coerceAtLeast(0.0),
                                "jumlahProtein" to (currentProtein - protein).coerceAtLeast(0.0),
                                "jumlahKarbohidrat" to (currentCarbs - carbs).coerceAtLeast(0.0),
                                "jumlahLemak" to (currentFat - fat).coerceAtLeast(0.0)
                            )

                            dbProgresGizi.child(userId).child(dateKey).updateChildren(updatedProgress).addOnSuccessListener {
                                callback(true, "Data makanan berhasil dihapus")
                            }.addOnFailureListener {
                                callback(false, "Failed to update progress: ${it.message}")
                            }
                        } else {
                            callback(false, "Progress data not found.")
                        }
                    }.addOnFailureListener {
                        callback(false, "Failed to fetch progress data: ${it.message}")
                    }
                }.addOnFailureListener {
                    callback(false, "Failed to delete food data: ${it.message}")
                }
            } else {
                callback(false, "Food data not found.")
            }
        }.addOnFailureListener {
            callback(false, "Failed to fetch food data: ${it.message}")
        }
    }

    fun editFoodAndProgress(
        userId: String,
        foodId: String,
        updatedFoodData: Map<String, Double?>, // Updated food parameters
        callback: (Boolean, String) -> Unit
    ) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var currentFoodData: Map<String, Double?>
        var currentProgress: Map<String, Double?>
        var reducedProgress: Map<String, Double?>
        var newProgress: Map<String, Double?>

        // ambil data makanan dari database
        dbMakanan.child(userId).child(dateKey).child(foodId).get().addOnSuccessListener { foodSnapshot ->
            if (foodSnapshot.exists()) {
                // masukin ke variabel lokal
                currentFoodData = mapOf(
                    "kalori" to (foodSnapshot.child("kalori").value?.toString()?.toDoubleOrNull() ?: 0.0),
                    "protein" to (foodSnapshot.child("protein").value?.toString()?.toDoubleOrNull() ?: 0.0),
                    "karbohidrat" to (foodSnapshot.child("karbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0),
                    "lemak" to (foodSnapshot.child("lemak").value?.toString()?.toDoubleOrNull() ?: 0.0)
                )

                // ambil data progres dari database
                dbProgresGizi.child(userId).child(dateKey).get().addOnSuccessListener { progressSnapshot ->
                    if (progressSnapshot.exists()) {
                        // masukin ke variabel lokal
                        currentProgress = mapOf(
                            "jumlahKalori" to (progressSnapshot.child("jumlahKalori").value?.toString()?.toDoubleOrNull() ?: 0.0),
                            "jumlahProtein" to (progressSnapshot.child("jumlahProtein").value?.toString()?.toDoubleOrNull() ?: 0.0),
                            "jumlahKarbohidrat" to (progressSnapshot.child("jumlahKarbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0),
                            "jumlahLemak" to (progressSnapshot.child("jumlahLemak").value?.toString()?.toDoubleOrNull() ?: 0.0)
                        )

                        // kurangin progres dengan makanan yang diambil
                        // lalu simpan ke variabel lokal yang baru
                        reducedProgress = currentProgress.mapValues { (key, value) ->
                            //(value?.minus((currentFoodData[key] ?: 0.0)))?.coerceAtLeast(0.0)
                            when (key) {
                                "jumlahKalori" -> value?.minus((currentFoodData["kalori"] ?: 0.0))
                                "jumlahProtein" -> value?.minus((currentFoodData["protein"] ?: 0.0))
                                "jumlahKarbohidrat" -> value?.minus((currentFoodData["karbohidrat"] ?: 0.0))
                                "jumlahLemak" -> value?.minus((currentFoodData["lemak"] ?: 0.0))
                                else -> value
                            }
                        }

                        // push data dari parameter ke database
                        dbMakanan.child(userId).child(dateKey).child(foodId).updateChildren(updatedFoodData).addOnSuccessListener {
                            // ambil data dari parameter
                            // lalu tambahkan ke progres yang sudah di kurangi sebelumnya
                            newProgress = reducedProgress.mapValues { (key, value) ->
                                when (key) {
                                    "jumlahKalori" -> value?.plus((updatedFoodData["kalori"] ?: 0.0))
                                    "jumlahProtein" -> value?.plus((updatedFoodData["protein"] ?: 0.0))
                                    "jumlahKarbohidrat" -> value?.plus((updatedFoodData["karbohidrat"] ?: 0.0))
                                    "jumlahLemak" -> value?.plus((updatedFoodData["lemak"] ?: 0.0))
                                    else -> value // Retain existing value for unmatched keys
                                }
                            }

                            // push data baru ke database
                            dbProgresGizi.child(userId).child(dateKey).updateChildren(newProgress).addOnSuccessListener {
                                callback(true, "Food and progress successfully updated.")
                            }.addOnFailureListener {
                                callback(false, "Failed to update progress: ${it.message}")
                            }
                        }.addOnFailureListener {
                            callback(false, "Failed to fetch food data: ${it.message}")
                        }
                    }
                }.addOnFailureListener {
                    callback(false, "Failed to fetch food data: ${it.message}")
                }
            }
        }.addOnFailureListener {
            callback(false, "Failed to fetch food data: ${it.message}")
        }
    }
}