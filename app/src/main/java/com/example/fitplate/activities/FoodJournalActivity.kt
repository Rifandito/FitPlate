package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.AuthManager
import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.example.fitplate.adapters.FoodAdapter
import com.example.fitplate.databinding.FoodJournalActivityBinding
import com.example.fitplate.dataclasses.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodJournalActivity : AppCompatActivity() {

    private lateinit var binding: FoodJournalActivityBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodAdapter
    private val foodList = mutableListOf<Makanan>() // List to store food data

    private val dbMakanan by lazy { RealtimeDatabase.instance().getReference("DataMakananUser") }
    private val dbProgresGizi by lazy { RealtimeDatabase.instance().getReference("ProgressGiziHarian") }
    private val dbTargetGizi by lazy { RealtimeDatabase.instance().getReference("TargetGiziHarian") }

    // ambil id dari shared preference
    private lateinit var authManager: AuthManager

    // variabel targetGizi dari database
    private var targetCalorie: Double? = null
    private var targetProtein: Double? = null
    private var targetKarbo: Double? = null
    private var targetLemak: Double? = null

    // variabel progresGizi dari database
    private var progressCalorie: Double? = null
    private var progressProtein: Double? = null
    private var progressKarbo: Double? = null
    private var progressLemak: Double? = null

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
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Ensure FoodJournalActivity is removed from the back stack
        }

        fetchFoodData(userId, getCurrentDate())
        fetchProgress(userId, getCurrentDate())
    }

    // fungsi ambil data makanan
    private fun fetchFoodData(userId: String, dateKey: String) {
        dbMakanan.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                foodList.clear()
                // perulangan
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
                //Toast.makeText(this, "tidak ada data pada hari ini", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Gagal mengambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.e("FoodJournalActivity", "Error: ${exception.message}")
        }
    }

    private var isGiziTargetLoaded = false
    private var isGiziProgressLoaded = false
    // fungsi ambil data progress
    private fun fetchProgress(userId: String, dateKey: String) {
        // Fetch target nutrition
        dbTargetGizi.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d("FirebaseDebug", "userId: $userId")
                targetCalorie = snapshot.child("targetKalori").value?.toString()?.toDoubleOrNull()
                targetProtein = snapshot.child("targetProtein").value?.toString()?.toDoubleOrNull()
                targetKarbo = snapshot.child("targetKarbohidrat").value?.toString()?.toDoubleOrNull()
                targetLemak = snapshot.child("targetLemak").value?.toString()?.toDoubleOrNull()

                Log.d("FirebaseDebug", "Target Lemak: $targetLemak")

                //Toast.makeText(this@HomeActivity, "user data fetched", Toast.LENGTH_SHORT).show()
            } else {
                // Additional data does not exist, navigate to InputUserActivity
                Toast.makeText(this, "Your gizi target data is not found", Toast.LENGTH_SHORT).show()
            }
            isGiziTargetLoaded = true
            checkDataLoaded()
        }

        // Fetch progress nutrition
        dbProgresGizi.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {

                // Fetch user inputs from the database
                progressCalorie = snapshot.child("jumlahKalori").value?.toString()?.toDoubleOrNull()
                progressProtein = snapshot.child("jumlahProtein").value?.toString()?.toDoubleOrNull()
                progressKarbo = snapshot.child("jumlahKarbohidrat").value?.toString()?.toDoubleOrNull()
                progressLemak = snapshot.child("jumlahLemak").value?.toString()?.toDoubleOrNull()
                //Toast.makeText(this@HomeActivity, "Gizi data fetched", Toast.LENGTH_SHORT).show()
            } else {
                // Additional data does not exist, navigate to InputUserActivity
                Toast.makeText(this, "Your gizi progress data is not found", Toast.LENGTH_SHORT).show()
            }
            isGiziProgressLoaded = true
            checkDataLoaded()
        }
    }

    private fun checkDataLoaded() {
        if (isGiziTargetLoaded && isGiziProgressLoaded) {
            updateNutritionUI()
        }
    }

    private fun updateNutritionUI() {
        binding.apply {
            textViewKaloriJournal.text = "${decimalFormat.format(progressCalorie ?: 0.0)}/${decimalFormat.format(targetCalorie ?: 0.0)} Kkal"
            textViewProteinJournal.text = "${decimalFormat.format(progressProtein ?: 0.0)}/${decimalFormat.format(targetProtein ?: 0.0)} Kkal"
            textViewKarboJournal.text = "${decimalFormat.format(progressKarbo ?: 0.0)}/${decimalFormat.format(targetKarbo ?: 0.0)} Kkal"
            textViewLemakJournal.text = "${decimalFormat.format(progressLemak ?: 0.0)}/${decimalFormat.format(targetLemak ?: 0.0)} Kkal"

            progressBarKaloriJournal.progress = calculateProgress(progressCalorie, targetCalorie)
            progressBarProteinJournal.progress = calculateProgress(progressProtein, targetProtein)
            progressBarKarboJournal.progress = calculateProgress(progressKarbo, targetKarbo)
            progressBarLemakJournal.progress = calculateProgress(progressLemak, targetLemak)

            // Update circular progress bar for calorie progress
            val overallProgress = calculateOverallProgress()
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

    private fun calculateOverallProgress(): Int {
        val calorieProgress = calculateProgress(progressCalorie, targetCalorie)
        val proteinProgress = calculateProgress(progressProtein, targetProtein)
        val carbProgress = calculateProgress(progressKarbo, targetKarbo)
        val fatProgress = calculateProgress(progressLemak, targetLemak)

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
}

