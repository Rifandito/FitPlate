package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.R

class DetailFoodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_food_activity)

        // Get data from intent
        val foodName = intent.getStringExtra("food_name") ?: ""
        val mealTime = intent.getStringExtra("meal_time") ?: ""
        val calories = intent.getIntExtra("calories", 0)
        val carbs = intent.getIntExtra("carbs", 0)
        val protein = intent.getIntExtra("protein", 0)
        val fat = intent.getIntExtra("fat", 0)

        // Initialize views
        val tvMealTime = findViewById<TextView>(R.id.tvMealTime)
        val tvFoodName = findViewById<TextView>(R.id.tvFoodName)
        val tvCalories = findViewById<TextView>(R.id.tvCalories)
        val tvCarbs = findViewById<TextView>(R.id.tvCarbs)
        val tvProtein = findViewById<TextView>(R.id.tvProtein)
        val tvFat = findViewById<TextView>(R.id.tvFat)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        // Set data to views
        tvMealTime.text = mealTime
        tvFoodName.text = foodName
        tvCalories.text = ": $calories kkal"
        tvCarbs.text = ": $carbs g"
        tvProtein.text = ": $protein g"
        tvFat.text = ": $fat g"

        // Handle back button
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Handle edit button
        btnEdit.setOnClickListener {
            // Tambahkan kode untuk edit
            val intent = Intent(this, FoodTrackerActivity::class.java).apply {
                putExtra("food_id", intent.getIntExtra("food_id", -1))
                putExtra("food_name", foodName)
                putExtra("calories", calories)
                putExtra("carbs", carbs)
                putExtra("protein", protein)
                putExtra("fat", fat)
            }
            startActivity(intent)
        }

        // Handle delete button
        btnDelete.setOnClickListener {
            // Tambahkan kode untuk delete
            AlertDialog.Builder(this)
                .setTitle("Hapus Makanan")
                .setMessage("Apakah Anda yakin ingin menghapus makanan ini?")
                .setPositiveButton("Ya") { _, _ ->
                    // Tambahkan kode untuk menghapus data
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }
}