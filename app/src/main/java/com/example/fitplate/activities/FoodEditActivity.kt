package com.example.fitplate.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.calculators.GiziProgressTracker
import com.example.fitplate.databinding.FoodEditActivityBinding
import com.example.fitplate.dataclasses.*
import java.text.SimpleDateFormat
import java.util.*

class FoodEditActivity : AppCompatActivity() {

    // inisialisasi view
    private lateinit var binding: FoodEditActivityBinding

//    private val dbMakanan by lazy { RealtimeDatabase.instance().getReference("DataMakananUser") }
//    private val dbProgresGizi by lazy { RealtimeDatabase.instance().getReference("ProgressGiziHarian") }

    // ambil id dari shared preference
    private lateinit var authManager: AuthManager

    private var currentDate: String = ""

    private var userId: String? = null
    // variable untuk nyimpen data yang di pass
    private var foodId: String? = null
    private var foodName: String? = null
    private var mealTime: String? = null
    private var calories: Double = 0.0
    private var carbs: Double = 0.0
    private var protein: Double = 0.0
    private var fat: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi binding
        binding = FoodEditActivityBinding.inflate(layoutInflater)
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

        // restore state if available
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Load data from intent
            loadIntentData()
        }

        // Handle the save button
        binding.editFoodButton.setOnClickListener {
            val updatedMakanan = getUpdatedData(userId!!, foodId!!)
            if (validateInput(updatedMakanan)) {
                saveUpdatedData(updatedMakanan)
            }
        }

        // button back ke home
        binding.FoodEditToDetail.setOnClickListener {
            //navigateToDetailFoodActivity()
            finish()
        }
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
        android.util.Log.d("FoodEditActivity", "loadIntentData: $foodName, $calories, $protein, $carbs, $fat")

        // Set data to views using binding
        updateUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        binding.apply {
            foodNameEdit.setText(foodName)
            kaloriEdit.setText(calories.toString())
            proteinEdit.setText(carbs.toString())
            karbohidratEdit.setText(protein.toString())
            lemakEdit.setText(fat.toString())
        }
    }

    private fun getUpdatedData(userId: String, foodId: String): Makanan {
        currentDate = getCurrentDate() // Get the current date and time

        val waktuMakanList = listOf("Sarapan", "Makan Siang", "Makan Sore", "Makan Malam")
        binding.dropdownWaktuMakanEdit.setAdapter(createAdapter(waktuMakanList))

        // ambil data dari input
        val foodName = binding.foodNameEdit.text.toString().trim()
        val kalori = binding.kaloriEdit.text.toString().toDoubleOrNull()
        val protein = binding.proteinEdit.text.toString().toDoubleOrNull()
        val karbo = binding.karbohidratEdit.text.toString().toDoubleOrNull()
        val lemak = binding.lemakEdit.text.toString().toDoubleOrNull()
        val waktuMakan = binding.dropdownWaktuMakanEdit.text.toString()

        return Makanan(
            idMakanan = foodId,
            idUser = userId,
            tanggal = currentDate, // Use the current date
            namaMakanan = foodName,
            waktuMakan = waktuMakan,
            kalori = kalori ?: 0.0,
            protein = protein ?: 0.0,
            karbohidrat = karbo ?: 0.0,
            lemak = lemak ?: 0.0
        )
    }

    private fun createAdapter(data: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, data)
    }

    // Validate input data
    private fun validateInput(makanan: Makanan): Boolean {
        when {
            makanan.namaMakanan.isEmpty() -> {
                Toast.makeText(this, "Nama makanan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            makanan.kalori == null -> {
                Toast.makeText(this, "Kalori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            makanan.protein == null -> {
                Toast.makeText(this, "Protein tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            makanan.karbohidrat == null -> {
                Toast.makeText(this, "Karbohidrat tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            makanan.lemak == null -> {
                Toast.makeText(this, "Lemak tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            makanan.waktuMakan.isEmpty() -> {
                Toast.makeText(this, "Waktu makan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

    private fun saveUpdatedData(updatedMakanan: Makanan) {
        val updatedData = mapOf(
            "kalori" to updatedMakanan.kalori,
            "protein" to updatedMakanan.protein,
            "karbohidrat" to updatedMakanan.karbohidrat,
            "lemak" to updatedMakanan.lemak
        )

        val progressTracker = GiziProgressTracker()
        progressTracker.editFoodAndProgress(userId!!, updatedMakanan.idMakanan, updatedData) { success, message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) {
                clearInputFields()
                val intent = Intent(this, FoodJournalActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Clear input fields after editing
    private fun clearInputFields() {
        binding.apply {
            foodNameEdit.text?.clear()
            kaloriEdit.text?.clear()
            proteinEdit.text?.clear()
            karbohidratEdit.text?.clear()
            lemakEdit.text?.clear()
        }
    }

    private fun navigateToDetailFoodActivity() {
        android.util.Log.d("FoodEditActivity", "Navigating to detail food with: $foodName, $calories, $protein, $carbs, $fat")
        val intent = Intent(this, DetailFoodActivity::class.java).apply {
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
        android.util.Log.d("FoodEditActivity", "onSaveInstanceState: $foodName, $calories, $protein, $carbs, $fat")
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
        android.util.Log.d("FoodEditActivity", "restoreState: $foodName, $calories, $protein, $carbs, $fat")

        // Update UI
        updateUI()
    }
}