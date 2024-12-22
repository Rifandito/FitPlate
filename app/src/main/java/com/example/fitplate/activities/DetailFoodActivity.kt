package com.example.fitplate.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.calculators.GiziProgressTracker
import com.example.fitplate.databinding.DetailFoodActivityBinding

class DetailFoodActivity : AppCompatActivity() {

    private lateinit var binding: DetailFoodActivityBinding
    private lateinit var authManager: AuthManager

    private var foodId: String? = null
    private var userId: String? = null
    private var foodName: String? = null
    private var mealTime: String? = null
    private var calories: Double = 0.0
    private var carbs: Double = 0.0
    private var protein: Double = 0.0
    private var fat: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi binding
        binding = DetailFoodActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        android.util.Log.d("DetailFoodActivity", "onCreate: savedInstanceState is ${if (savedInstanceState == null) "null" else "not null"}")

        // Initialize AuthManager
        authManager = AuthManager(this)
        // Get user ID
        userId = authManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // restore state if available
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Load data from intent
            loadIntentData()
        }
        // Set up button listeners
        setupListeners()
    }

    private fun loadIntentData() {
        val intent = intent
        foodId = intent.getStringExtra("food_id")
        foodName = intent.getStringExtra("food_name") ?: ""
        mealTime = intent.getStringExtra("meal_time") ?: ""
        calories = intent.getDoubleExtra("calories", 0.0)
        carbs = intent.getDoubleExtra("carbs", 0.0)
        protein = intent.getDoubleExtra("protein", 0.0)
        fat = intent.getDoubleExtra("fat", 0.0)

        // Log the received data
        android.util.Log.d("DetailFoodActivity", "loadIntentData: $foodName, $calories, $protein, $carbs, $fat")

        // Set data to views using binding
        updateUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
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
        android.util.Log.d("DetailFoodActivity", "Navigating to edit with: $foodName, $calories, $protein, $carbs, $fat")
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
        val progressTracker = GiziProgressTracker()
        progressTracker.reduceDailyProgress(userId!!, foodId!!) { success, message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) navigateBackToJournal()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("food_id", foodId)
        outState.putString("food_name", foodName)
        outState.putString("meal_time", mealTime)
        outState.putDouble("calories", calories)
        outState.putDouble("carbs", carbs)
        outState.putDouble("protein", protein)
        outState.putDouble("fat", fat)

        // Log the saved state
        android.util.Log.d("DetailFoodActivity", "onSaveInstanceState: $foodName, $calories, $protein, $carbs, $fat")
    }

    private fun restoreState(savedInstanceState: Bundle) {
        foodId = savedInstanceState.getString("food_id")
        foodName = savedInstanceState.getString("food_name")
        mealTime = savedInstanceState.getString("meal_time")
        calories = savedInstanceState.getDouble("calories", 0.0)
        carbs = savedInstanceState.getDouble("carbs", 0.0)
        protein = savedInstanceState.getDouble("protein", 0.0)
        fat = savedInstanceState.getDouble("fat", 0.0)

        // Log the restored state
        android.util.Log.d("DetailFoodActivity", "restoreState: $foodName, $calories, $protein, $carbs, $fat")

        // Update UI
        updateUI()
    }
}