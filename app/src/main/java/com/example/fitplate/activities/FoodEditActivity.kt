package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.RealtimeDatabase
import com.example.fitplate.databinding.FoodEditActivityBinding
import com.example.fitplate.dataclasses.*
import java.text.SimpleDateFormat
import java.util.*

class FoodEditActivity : AppCompatActivity() {

    // inisialisasi view
    private lateinit var binding: FoodEditActivityBinding

    private val dbMakanan by lazy { RealtimeDatabase.instance().getReference("DataMakananUser") }
    private val dbProgresGizi by lazy { RealtimeDatabase.instance().getReference("ProgressGiziHarian") }

    // ambil id dari shared preference
    private lateinit var authManager: AuthManager

    private var currentDate: String = ""

    private var userId: String? = null
    // variable untuk nyimpen data yang di pass
    private var foodId: String? = null
    private var foodName: String? = null
    private var mealTime: String? = null
    private var calories: Double? = null
    private var carbs: Double? = null
    private var protein: Double? = null
    private var fat: Double? = null

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

        // Load data from intent
        loadIntentData()

        // Handle the save button
        binding.editFoodButton.setOnClickListener {
            val updatedMakanan = getUpdatedData(userId!!, foodId!!)
            if (validateInput(updatedMakanan)) {
                saveUpdatedData(updatedMakanan)
            }
        }

        // button back ke home
        binding.FoodEditToDetail.setOnClickListener {
            val intent = Intent(this, FoodJournalActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Ensure FoodJournalActivity is removed from the back stack
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

        // Populate input fields
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
        }
        return true
    }

    private fun saveUpdatedData(updatedMakanan: Makanan) {
        val dateKey = getCurrentDate()

        dbMakanan.child(updatedMakanan.idUser).child(dateKey).child(updatedMakanan.idMakanan).get().addOnSuccessListener { snapshot ->
            val oldMakanan = snapshot.getValue(Makanan::class.java)

            // Save the updated data
            dbMakanan.child(updatedMakanan.idUser).child(dateKey).child(updatedMakanan.idMakanan)
                .setValue(updatedMakanan)
                .addOnSuccessListener {
                    updateGiziHarian(updatedMakanan.idUser, oldMakanan, updatedMakanan)
                    clearInputFields()
                    Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data lama", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateProgress(newValue: Double?, existingValue: Double?): Double {
        return (existingValue ?: 0.0) + (newValue ?: 0.0)
    }

    // Fungsi update progress gizi
    private fun updateGiziHarian(userId: String) {
//        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//        // Fetch makanan data for the specific date
//        dbMakanan.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
//            if (snapshot.exists()) {
//                // Reset values to avoid accumulation across multiple calls
//                totalCalorie = 0.0
//                totalProtein = 0.0
//                totalKarbo = 0.0
//                totalLemak = 0.0
//
//                for (foodSnapshot in snapshot.children) {
//                    val banyakCalorie = foodSnapshot.child("kalori").value?.toString()?.toDoubleOrNull() ?: 0.0
//                    val banyakProtein = foodSnapshot.child("protein").value?.toString()?.toDoubleOrNull() ?: 0.0
//                    val banyakKarbo = foodSnapshot.child("karbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
//                    val banyakLemak = foodSnapshot.child("lemak").value?.toString()?.toDoubleOrNull() ?: 0.0
//
//                    // Accumulate the values
//                    totalCalorie = calculateProgress(banyakCalorie, totalCalorie)
//                    totalProtein = calculateProgress(banyakProtein, totalProtein)
//                    totalKarbo = calculateProgress(banyakKarbo, totalKarbo)
//                    totalLemak = calculateProgress(banyakLemak, totalLemak)
//                }
//
//                // Convert to Map
//                val progressGiziHarianMap = mapOf(
//                    "jumlahKalori" to totalCalorie,
//                    "jumlahKarbohidrat" to totalKarbo,
//                    "jumlahProtein" to totalProtein,
//                    "jumlahLemak" to totalLemak,
//                    "status" to "progress saved"
//                )
//
//                //Toast.makeText(this, "Data makanan berhasil diambil dari database", Toast.LENGTH_SHORT).show()
//                Log.d("FoodTrackerActivity", "Total Kalori: $totalCalorie")
//                Log.d("FoodTrackerActivity", "Total Protein: $totalProtein")
//                Log.d("FoodTrackerActivity", "Total Karbohidrat: $totalKarbo")
//                Log.d("FoodTrackerActivity", "Total Lemak: $totalLemak")
//
//                // Update atau buat data progress harian
//                dbProgresGizi.child(userId).child(dateKey).updateChildren(progressGiziHarianMap)
//                    .addOnSuccessListener {
//                        //Toast.makeText(this, "Progress gizi berhasil diperbarui", Toast.LENGTH_SHORT).show()
//                    }
//                    .addOnFailureListener { exception ->
//                        Toast.makeText(this, "Gagal memperbarui progress gizi: ${exception.message}", Toast.LENGTH_SHORT).show()
//                        Log.e("FoodTrackerActivity", "Error: ${exception.message}")
//                    }
//            } else {
//                Toast.makeText(this, "Tidak ada data makanan untuk tanggal ini", Toast.LENGTH_SHORT).show()
//            }
//        }.addOnFailureListener { exception ->
//            Toast.makeText(this, "Gagal mengambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
//            Log.e("FoodTrackerActivity", "Error: ${exception.message}")
//        }
    }

    private fun updateGiziHarian(userId: String, oldMakanan: Makanan?, newMakanan: Makanan) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        dbProgresGizi.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Fetch current progress values
                val currentCalories = snapshot.child("jumlahKalori").value?.toString()?.toDoubleOrNull() ?: 0.0
                val currentProtein = snapshot.child("jumlahProtein").value?.toString()?.toDoubleOrNull() ?: 0.0
                val currentCarbs = snapshot.child("jumlahKarbohidrat").value?.toString()?.toDoubleOrNull() ?: 0.0
                val currentFat = snapshot.child("jumlahLemak").value?.toString()?.toDoubleOrNull() ?: 0.0

                // Subtract old values (if oldMakanan exists)
                val updatedCalories = (currentCalories - (oldMakanan?.kalori ?: 0.0) + newMakanan.kalori!!).coerceAtLeast(0.0)
                val updatedProtein = (currentProtein - (oldMakanan?.protein ?: 0.0) + newMakanan.protein!!).coerceAtLeast(0.0)
                val updatedCarbs = (currentCarbs - (oldMakanan?.karbohidrat ?: 0.0) + newMakanan.karbohidrat!!).coerceAtLeast(0.0)
                val updatedFat = (currentFat - (oldMakanan?.lemak ?: 0.0) + newMakanan.lemak!!).coerceAtLeast(0.0)

                // Update progress in the database
                val progressMap = mapOf(
                    "jumlahKalori" to updatedCalories,
                    "jumlahProtein" to updatedProtein,
                    "jumlahKarbohidrat" to updatedCarbs,
                    "jumlahLemak" to updatedFat
                )

                dbProgresGizi.child(userId).child(dateKey).updateChildren(progressMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Progress berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal memperbarui progress gizi", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Progress gizi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil progress gizi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Clear input fields after registration
    private fun clearInputFields() {
        binding.apply {
            foodNameEdit.text?.clear()
            kaloriEdit.text?.clear()
            proteinEdit.text?.clear()
            karbohidratEdit.text?.clear()
            lemakEdit.text?.clear()
        }
    }
}
