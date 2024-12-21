package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.R
import com.example.fitplate.adapters.FoodAdapter
import com.example.fitplate.RecyclerItems.FoodItem

class FoodJournalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.food_journal_activity)

        fun onBackPressed() {
            super.onBackPressed() // Menyelesaikan aktivitas ini dan kembali ke layar sebelumnya
        }

        recyclerView = findViewById(R.id.foodRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data
        val foodList = listOf(
            FoodItem(1, "Ayam Bakar", "Makan Siang", 300, 20, 35, 15),
            FoodItem(2, "Nasi Goreng", "Makan Malam", 450, 65, 15, 12)
            // Add more items as needed
        )

        adapter = FoodAdapter(foodList) { food ->
            // Handle item click
            val intent = Intent(this, DetailFoodActivity::class.java).apply {
                putExtra("food_id", food.id)
                putExtra("food_name", food.name)
                putExtra("meal_time", food.mealTime)
                putExtra("calories", food.calories)
                putExtra("carbs", food.carbs)
                putExtra("protein", food.protein)
                putExtra("fat", food.fat)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }
}

//class FoodJournalActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.food_journal_activity) // Replace with your actual layout file
//    }
//
//    fun openFoodJournalActivity(view: View) {
//        val intent = Intent(this, FoodJournalActivity::class.java)
//        startActivity(intent)
//    }
//
//}

