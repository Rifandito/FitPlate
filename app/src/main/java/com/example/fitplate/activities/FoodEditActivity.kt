package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.R
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

    // variabel progressGizi dari database
    private var totalCalorie: Double? = null
    private var totalProtein: Double? = null
    private var totalKarbo: Double? = null
    private var totalLemak: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.food_tracker_activity)

        // Inisialisasi binding
        binding = FoodEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AuthManager
        authManager = AuthManager(this)

        // Get user ID
        val userId = authManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Button to add food data
        binding.editFoodButton.setOnClickListener {
            val dataMakanan = getInputData(userId)
            if (validateInput(dataMakanan)) {
                saveDataToDatabase(dataMakanan)

                // Open FoodJournalActivity
                val intent = Intent(this, FoodJournalActivity::class.java)
                startActivity(intent)
            }
        }

        // button back ke home
        binding.FoodTrackToHome.setOnClickListener {
            val intent = Intent(this, FoodJournalActivity::class.java)
            startActivity(intent)
        }
    }

    //fungsi untuk ngambil data dari input, kemudian dimasukkan ke objek
    private fun getInputData(userId: String): Makanan {
        currentDate = getCurrentDate() // Get the current date and time

        // ambil data dari input
        val foodName = binding.foodNameInput.text.toString().trim()
        val kalori = binding.kaloriInput.text.toString().toDoubleOrNull()
        val protein = binding.proteinInput.text.toString().toDoubleOrNull()
        val karbo = binding.karbohidratInput.text.toString().toDoubleOrNull()
        val lemak = binding.lemakInput.text.toString().toDoubleOrNull()

        // masukkan ke objek
        return Makanan(
            idMakanan = UUID.randomUUID().toString(),
            idUser = userId,
            tanggal = currentDate,
            waktuMakan = "",
            namaMakanan = foodName,
            kalori = kalori,
            protein = protein,
            karbohidrat = karbo,
            lemak = lemak
        )
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
        }
        return true
    }

    // Fungsi save data makanan
    private fun saveDataToDatabase(dataMakanan: Makanan) {
        dbMakanan.child(dataMakanan.idUser).child(dataMakanan.tanggal).child(dataMakanan.idMakanan).setValue(dataMakanan)
            .addOnSuccessListener {
                clearInputFields()
                updateGiziHarian(dataMakanan.idUser)
                //Toast.makeText(this, "data makanan berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Makanan yang anda konsumsi gagal direkam", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateProgress(newValue: Double?, existingValue: Double?): Double {
        return (existingValue ?: 0.0) + (newValue ?: 0.0)
    }

    // Fungsi update progress gizi
    private fun updateGiziHarian(userId: String) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Fetch makanan data for the specific date
        dbMakanan.child(userId).child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Reset values to avoid accumulation across multiple calls
                totalCalorie = 0.0
                totalProtein = 0.0
                totalKarbo = 0.0
                totalLemak = 0.0

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

                // Convert to Map
                val progressGiziHarianMap = mapOf(
                    "jumlahKalori" to totalCalorie,
                    "jumlahKarbohidrat" to totalKarbo,
                    "jumlahProtein" to totalProtein,
                    "jumlahLemak" to totalLemak,
                    "status" to "progress saved"
                )

                //Toast.makeText(this, "Data makanan berhasil diambil dari database", Toast.LENGTH_SHORT).show()
                Log.d("FoodTrackerActivity", "Total Kalori: $totalCalorie")
                Log.d("FoodTrackerActivity", "Total Protein: $totalProtein")
                Log.d("FoodTrackerActivity", "Total Karbohidrat: $totalKarbo")
                Log.d("FoodTrackerActivity", "Total Lemak: $totalLemak")

                // Update atau buat data progress harian
                dbProgresGizi.child(userId).child(dateKey).updateChildren(progressGiziHarianMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Progress gizi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Gagal memperbarui progress gizi: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("FoodTrackerActivity", "Error: ${exception.message}")
                    }
            } else {
                Toast.makeText(this, "Tidak ada data makanan untuk tanggal ini", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Gagal mengambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.e("FoodTrackerActivity", "Error: ${exception.message}")
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Clear input fields after registration
    private fun clearInputFields() {
        binding.apply {
            foodNameInput.text?.clear()
            kaloriInput.text?.clear()
            proteinInput.text?.clear()
            karbohidratInput.text?.clear()
            lemakInput.text?.clear()
        }
    }
}