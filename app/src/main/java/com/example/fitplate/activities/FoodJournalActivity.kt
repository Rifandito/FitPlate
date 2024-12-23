package com.example.fitplate.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.AuthManager
import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.example.fitplate.adapters.FoodAdapter
import com.example.fitplate.calculators.GiziProgressTracker
import com.example.fitplate.databinding.FoodJournalActivityBinding
import com.example.fitplate.dataclasses.*
import com.google.firebase.database.*
import java.text.*
import java.util.*

class FoodJournalActivity : AppCompatActivity() {

    private lateinit var binding: FoodJournalActivityBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodAdapter
    private val foodList = mutableListOf<Makanan>() // List to store food data

    private val dbMakanan by lazy { RealtimeDatabase.instance().getReference("DataMakananUser") }
    private val dbTargetGizi by lazy { RealtimeDatabase.instance().getReference("TargetGiziHarian") }

    // ambil id dari shared preference
    private lateinit var authManager: AuthManager

    // variabel targetGizi dari database
    private var targetCalorie: Double? = null
    private var targetProtein: Double? = null
    private var targetKarbo: Double? = null
    private var targetLemak: Double? = null

    private val decimalFormat = DecimalFormat("#.0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi binding
        binding = FoodJournalActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.foodRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FoodAdapter(foodList) { makanan ->
            // Handle item click - navigate to DetailFoodActivity
            val intent = Intent(this, DetailFoodActivity::class.java).apply {
                putExtra("food_id", makanan.idMakanan)
                putExtra("food_name", makanan.namaMakanan)
                putExtra("meal_time", makanan.waktuMakan)
                putExtra("calories", makanan.kalori)
                putExtra("carbs", makanan.karbohidrat)
                putExtra("protein", makanan.protein)
                putExtra("fat", makanan.lemak)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Initialize AuthManager
        authManager = AuthManager(this)
        // Get user ID
        val userId = authManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.buttonAddProgress.setOnClickListener {
            val intent = Intent(this, FoodTrackerActivity::class.java)
            startActivity(intent)
        }

        binding.foodJournalToHome.setOnClickListener {
            finish() // Ensure FoodJournalActivity is removed from the back stack
        }

        fetchFoodData(userId, getCurrentDate())
        fetchProgress(userId)
    }

    private fun fetchFoodData(userId: String, dateKey: String) {
        dbMakanan.child(userId).child(dateKey).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()
                if (snapshot.exists()) {
                    for (foodSnapshot in snapshot.children) {
                        val makanan = Makanan(
                            idMakanan = foodSnapshot.key ?: "",
                            idUser = userId,
                            tanggal = dateKey,
                            namaMakanan = foodSnapshot.child("namaMakanan").value?.toString() ?: "",
                            waktuMakan = foodSnapshot.child("waktuMakan").value?.toString() ?: "",
                            kalori = foodSnapshot.child("kalori").value?.toString()?.toDoubleOrNull() ?: 0.0,
                            protein = foodSnapshot.child("protein").value?.toString()?.toDoubleOrNull() ?: 0.0,
                            karbohidrat = foodSnapshot.child("karbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0,
                            lemak = foodSnapshot.child("lemak").value?.toString()?.toDoubleOrNull() ?: 0.0
                        )
                        foodList.add(makanan)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    //Toast.makeText(this@FoodJournalActivity, "Kamu belum ada progress", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodJournalActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var isGiziTargetLoaded = false
    private var isGiziProgressLoaded = false
    private var fetchedProgressData: Map<String, Double> = mapOf()
    private fun fetchProgress(userId: String) {
        // Fetch target nutrition
        dbTargetGizi.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    targetCalorie = snapshot.child("targetKalori").value?.toString()?.toDoubleOrNull()
                    targetProtein = snapshot.child("targetProtein").value?.toString()?.toDoubleOrNull()
                    targetKarbo = snapshot.child("targetKarbohidrat").value?.toString()?.toDoubleOrNull()
                    targetLemak = snapshot.child("targetLemak").value?.toString()?.toDoubleOrNull()

                    isGiziTargetLoaded = true
                    checkDataLoaded()
                } else {
                    Toast.makeText(this@FoodJournalActivity, "Your gizi target data is not found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodJournalActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        val progressTracker = GiziProgressTracker()
        progressTracker.fetchDailyProgress(userId, object : GiziProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Double>) {
                fetchedProgressData = data
                isGiziProgressLoaded = true
                checkDataLoaded()
            }
            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@FoodJournalActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkDataLoaded() {
        if (isGiziTargetLoaded && isGiziProgressLoaded) {
            updateNutritionUI(fetchedProgressData)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNutritionUI(data: Map<String, Double>) {
        binding.apply {
            textViewKaloriJournal.text = "${decimalFormat.format(data["jumlahKalori"] ?: 0.0)}/${decimalFormat.format(targetCalorie ?: 0.0)} Kkal"
            textViewProteinJournal.text = "${decimalFormat.format(data["jumlahProtein"] ?: 0.0)}/${decimalFormat.format(targetProtein ?: 0.0)} Kkal"
            textViewKarboJournal.text = "${decimalFormat.format(data["jumlahKarbohidrat"] ?: 0.0)}/${decimalFormat.format(targetKarbo ?: 0.0)} Kkal"
            textViewLemakJournal.text = "${decimalFormat.format(data["jumlahLemak"] ?: 0.0)}/${decimalFormat.format(targetLemak ?: 0.0)} Kkal"

            progressBarKaloriJournal.progress = calculateProgress(data["jumlahKalori"], targetCalorie)
            progressBarProteinJournal.progress = calculateProgress(data["jumlahProtein"], targetProtein)
            progressBarKarboJournal.progress = calculateProgress(data["jumlahKarbohidrat"], targetKarbo)
            progressBarLemakJournal.progress = calculateProgress(data["jumlahLemak"], targetLemak)

            // Update circular progress bar for calorie progress
            val overallProgress = calculateOverallProgress(data)
            progressCircle.progress = overallProgress

            // Update label for the circular bar
            textViewProgressCircle.text = "secara keseluruhan ${overallProgress}%"
        }
    }

    private fun calculateProgress(progress: Double?, target: Double?): Int {
        return if (progress != null && target != null && target > 0) {
            ((progress / target) * 100).toInt()
        } else {
            0
        }
    }

    private fun calculateOverallProgress(data: Map<String, Double>): Int {
        val calorieProgress = calculateProgress(data["jumlahKalori"], targetCalorie)
        val proteinProgress = calculateProgress(data["jumlahProtein"], targetProtein)
        val carbProgress = calculateProgress(data["jumlahKarbohidrat"], targetKarbo)
        val fatProgress = calculateProgress(data["jumlahLemak"], targetLemak)

        // Calculate overall progress (each contributes 25%)
        val overallProgress = (
                (calorieProgress * 0.25) +
                        (proteinProgress * 0.25) +
                        (carbProgress * 0.25) +
                        (fatProgress * 0.25)
                ).toInt()
        return overallProgress
    }

    // fungsi ambil data tanggal
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onResume() {
        super.onResume()
        // Refresh the food list when returning to this activity
        val userId = authManager.getUserId() ?: return
        fetchFoodData(userId, getCurrentDate())
    }
}