package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.RealtimeDatabase
import com.example.fitplate.databinding.DetailFoodActivityBinding

class DetailFoodActivity : AppCompatActivity() {

    private lateinit var binding: DetailFoodActivityBinding

    private val dbMakanan by lazy { RealtimeDatabase.instance().getReference("DataMakananUser") }
    private val dbProgresGizi by lazy { RealtimeDatabase.instance().getReference("ProgressGiziHarian") }

    private lateinit var authManager: AuthManager
    private var foodId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi binding
        binding = DetailFoodActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AuthManager
        authManager = AuthManager(this)
        // Get user ID
        userId = authManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load data from intent
        loadIntentData()

        // Set up button listeners
        setupListeners()
    }

    private fun loadIntentData() {
        val intent = intent
        foodId = intent.getStringExtra("food_id")
        val foodName = intent.getStringExtra("food_name") ?: ""
        val mealTime = intent.getStringExtra("meal_time") ?: ""
        val calories = intent.getDoubleExtra("calories", 0.0)
        val carbs = intent.getDoubleExtra("carbs", 0.0)
        val protein = intent.getDoubleExtra("protein", 0.0)
        val fat = intent.getDoubleExtra("fat", 0.0)

        // Set data to views using binding
        binding.apply {
            tvMealTime.text = mealTime
            tvFoodName.text = foodName
            tvCalories.text = ": $calories kkal"
            tvCarbs.text = ": $carbs g"
            tvProtein.text = ": $protein g"
            tvFat.text = ": $fat g"
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener { navigateBackToJournal() }
            btnEdit.setOnClickListener { navigateToEditActivity() }
            btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
        }
    }

    private fun navigateBackToJournal() {
        val intent = Intent(this, FoodJournalActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToEditActivity() {
        val foodName = binding.tvFoodName.text.toString()
        val mealTime = binding.tvMealTime.text.toString()
        val calories = binding.tvCalories.text.removePrefix(": ").removeSuffix(" kkal").toString().toDouble()
        val carbs = binding.tvCarbs.text.removePrefix(": ").removeSuffix(" g").toString().toDouble()
        val protein = binding.tvProtein.text.removePrefix(": ").removeSuffix(" g").toString().toDouble()
        val fat = binding.tvFat.text.removePrefix(": ").removeSuffix(" g").toString().toDouble()

        val intent = Intent(this, FoodEditActivity::class.java).apply {
            putExtra("food_id", foodId)
            putExtra("food_name", foodName)
            putExtra("meal_time", mealTime)
            putExtra("calories", calories)
            putExtra("carbs", carbs)
            putExtra("protein", protein)
            putExtra("fat", fat)
        }
        startActivity(intent)
    }


    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Makanan")
            .setMessage("Apakah Anda yakin ingin menghapus makanan ini?")
            .setPositiveButton("Ya") { _, _ -> deleteFoodData() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteFoodData() {
        if (foodId == null || userId == null) {
            Toast.makeText(this, "Error: Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val dateKey = getCurrentDate()
        // Fetch the food data first to adjust the progress
        dbMakanan.child(userId!!).child(dateKey).child(foodId!!).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Extract nutrient values from the food item
                val calories = snapshot.child("kalori").value?.toString()?.toDoubleOrNull() ?: 0.0
                val protein = snapshot.child("protein").value?.toString()?.toDoubleOrNull() ?: 0.0
                val carbs = snapshot.child("karbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
                val fat = snapshot.child("lemak").value?.toString()?.toDoubleOrNull() ?: 0.0

                // Remove the food item
                dbMakanan.child(userId!!).child(dateKey).child(foodId!!).removeValue().addOnSuccessListener {
                    // Update progress after deletion
                    updateProgressAfterDeletion(userId!!, dateKey, calories, protein, carbs, fat)

                    Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    navigateBackToJournal()
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Data makanan tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data makanan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProgressAfterDeletion(
        userId: String,
        dateKey: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double
    ) {
        dbProgresGizi.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Fetch current progress values
                val currentCalories = snapshot.child("jumlahKalori").value?.toString()?.toDoubleOrNull() ?: 0.0
                val currentProtein = snapshot.child("jumlahProtein").value?.toString()?.toDoubleOrNull() ?: 0.0
                val currentCarbs = snapshot.child("jumlahKarbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
                val currentFat = snapshot.child("jumlahLemak").value?.toString()?.toDoubleOrNull() ?: 0.0

                // Calculate new progress values after deletion
                val updatedCalories = (currentCalories - calories).coerceAtLeast(0.0)
                val updatedProtein = (currentProtein - protein).coerceAtLeast(0.0)
                val updatedCarbs = (currentCarbs - carbs).coerceAtLeast(0.0)
                val updatedFat = (currentFat - fat).coerceAtLeast(0.0)

                // Update progress in the database
                val updatedProgressMap = mapOf(
                    "jumlahKalori" to updatedCalories,
                    "jumlahProtein" to updatedProtein,
                    "jumlahKarbohidrat" to updatedCarbs,
                    "jumlahLemak" to updatedFat
                )

                dbProgresGizi.child(userId).child(dateKey).updateChildren(updatedProgressMap)
                    .addOnSuccessListener {
                        //Toast.makeText(this, "Progress berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal memperbarui progress", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Progress gizi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil progress gizi", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
}