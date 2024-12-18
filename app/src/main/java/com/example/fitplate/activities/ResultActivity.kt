package com.example.fitplate.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.calculators.*

import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ResultActivity : AppCompatActivity() {

    private lateinit var tujuanDiet: TextView
    private lateinit var intensitasOlahraga: TextView
    private lateinit var jeniskelamin: TextView
    private lateinit var tingkatanProgram: TextView
    private lateinit var usia: TextView
    private lateinit var konsumsiAir: TextView
    private lateinit var jumlahMakan: TextView
    private lateinit var tinggiBadan: TextView
    private lateinit var beratBadan: TextView
    private lateinit var targetBb: TextView
    private lateinit var hasilBMI: TextView

    private val dbUser by lazy { RealtimeDatabase.instance().getReference("users") }
    private val dbTargetGiziHarian by lazy { RealtimeDatabase.instance().getReference("TargetGiziHarian") }
    private val dbTargetKonsumsiAir by lazy { RealtimeDatabase.instance().getReference("TargetKonsumsiAir") }

    private lateinit var authManager: AuthManager

    // Declare variables for user data
    private lateinit var iDuser: String
    private var gender: String? = null
    private var weight: Double? = null
    private var height: Double? = null
    private var tingkatan: String? = null
    private var age: Int? = null
    private var activityLevel: String? = null
    private var dietGoal: String? = null
    private var targetBeratBadan: Double? = null
    private var konsumsiAIR: Double? = null
    private var brpKaliMakan: Int? = null

    // Calculation results
    private var bmiResult: Double? = null
    private var bmrResult: Double? = null
    private var tdeeResult: Double? = null
    private var adjustedTdeeResult: Double? = null
    private var waterIntakeResult: Double? = null
    private var macronutrientsResult: Map<String, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Initialize AuthManager
        authManager = AuthManager(this)

        // Get user ID
        val userId = authManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // inisialisasi view
        inisialisasiView()

        // Step 1: Fetch user data -> all methods are called from here
        getDataFromDatabase(userId)

        // Button to navigate to HomeActivity
        val btnMulaiSekarang: Button = findViewById(R.id.btnMulaiSekarang)
        btnMulaiSekarang.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    //inisialisasi view
    private fun inisialisasiView(){
        tujuanDiet = findViewById(R.id.tvTujuanDiet)
        intensitasOlahraga = findViewById(R.id.tvOlahraga)
        jeniskelamin = findViewById(R.id.tvGender)
        tingkatanProgram = findViewById(R.id.tvTingkatan)
        usia = findViewById(R.id.tvUsia)
        konsumsiAir = findViewById(R.id.tvKonsumsiAir)
        jumlahMakan = findViewById(R.id.tvJumlahMakan)
        tinggiBadan = findViewById(R.id.tvTinggiBadan)
        beratBadan = findViewById(R.id.tvBeratBadan)
        targetBb = findViewById(R.id.tvTargetBB)
        hasilBMI = findViewById(R.id.tvBMI)
    }

    // fungsi untuk mengambil data user dari database
    private fun getDataFromDatabase(userId: String) {
        dbUser.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Fetch user inputs from the database
                    iDuser = snapshot.child("idUser").value.toString()
                    gender = snapshot.child("jenisKelamin").value?.toString() ?: "unknown"
                    weight = snapshot.child("beratBadan").value?.toString()?.toDoubleOrNull() ?: return
                    height = snapshot.child("tinggiBadan").value?.toString()?.toDoubleOrNull() ?: return
                    tingkatan = snapshot.child("tingkatKesulitan").value?.toString() ?: "beginner"
                    age = snapshot.child("usia").value?.toString()?.toIntOrNull() ?: return
                    activityLevel = snapshot.child("intensitasOlahraga").value?.toString() ?: "Tidak Pernah"
                    dietGoal = snapshot.child("tujuanDiet").value?.toString()?.lowercase() ?: "maintenance"
                    targetBeratBadan = snapshot.child("targetBb").value?.toString()?.toDoubleOrNull() ?: 0.0
                    konsumsiAIR = snapshot.child("konsumsiAirUser").value?.toString()?.toDoubleOrNull() ?: 0.0
                    brpKaliMakan = snapshot.child("frekuensiMakan").value?.toString()?.toIntOrNull() ?: return

                    // step 2, display all data except bmi
                    displayUserData()

                    // step 3, perform calculations
                    getCalculationResult()

                    // Step 4, Save calculation results to the database
                    saveBMItoUserDatabase()
                    saveTargetGiziHarianToDatabase()
                    saveTargetKonsumsiAirToDatabase()

                    // Step 5, Fetch and display saved BMI
                    showUserBMI()

                    Toast.makeText(this@ResultActivity, "user data fetched", Toast.LENGTH_SHORT).show()
                } else {
                    // Additional data does not exist, navigate to InputUserActivity
                    Toast.makeText(this@ResultActivity, "user data is not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResultActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayUserData() {
        tujuanDiet.text = dietGoal ?: "N/A"
        intensitasOlahraga.text = activityLevel ?: "N/A"
        jeniskelamin.text = gender ?: "N/A"
        tingkatanProgram.text = tingkatan
        usia.text = (age ?: "N/A").toString()
        konsumsiAir.text = (konsumsiAIR ?: "N/A").toString()
        jumlahMakan.text = (brpKaliMakan ?: "N/A").toString()
        tinggiBadan.text = (height ?: "N/A").toString()
        beratBadan.text = (weight ?: "N/A").toString()
        targetBb.text = (targetBeratBadan ?: "N/A").toString()
    }

    // fungsi untuk menghitung BMI, BMR, dll menggunakan calculator class
    private fun getCalculationResult() {
        // bagian kalkulator bmi
        bmiResult = weight.let { weight ->
            height.let { height ->
                Calculators.calculateBMI(weight, height)
            }
        }

        // bagian kalkulator bmr
        bmrResult = Calculators.calculateBMR(
            gender = gender ?: "",
            weight = weight ?: 0.0,
            height = height ?: 0.0,
            age = age ?: 0
        )

        // bagian kalkulator tdee
        tdeeResult = bmrResult?.let { bmr ->
            activityLevel.let { aktivitas ->
                Calculators.calculateTDEE(bmr, aktivitas)
            } //?: Calculators.calculateTDEE(bmr, "tidak pernah") // Default value
        }

        // Bagian kalkulator Adjusted TDEE
        adjustedTdeeResult = tdeeResult?.let { tdee ->
            dietGoal?.let { tujuanDiet ->
                val userLevel = tingkatan ?: "beginner" // Replace with the actual user level if stored separately
                Calculators.calculateAdjustedTDEE(tdee, tujuanDiet, userLevel)
            } ?: Calculators.calculateAdjustedTDEE(tdee, "jaga berat badan", "beginner") // Default value for dietGoal and level
        }


        // Bagian kalkulator Water Intake
        // ADA yang harus diubah
        waterIntakeResult = weight.let { weight ->
            activityLevel.let { olahraga ->
                val exerciseMinutes = when (olahraga?.lowercase()) {
                    "1-2 hari/minggu" -> 30
                    "3-5 hari/minggu" -> 60
                    "6-7 hari/minggu" -> 90
                    else -> 0 // Default: No exercise
                }
                Calculators.calculateWaterIntake(weight, exerciseMinutes)
            } ?: Calculators.calculateWaterIntake(weight, 0) // Default if intensitasOlahraga is null
        }

        // Bagian kalkulator Macronutrient
        macronutrientsResult = adjustedTdeeResult?.let { adjustedTdee ->
            dietGoal.let { goal ->
                Calculators.calculateMacronutrients(adjustedTdee, goal)
            } //?: Calculators.calculateMacronutrients(adjustedTdee, "jaga berat badan") // Default goal
        }
    }

    // fungsi untuk menyimpan hasil kalkulasi BMI ke database user
    private fun saveBMItoUserDatabase() {
        bmiResult?.let { bmi ->
            dbUser.child(iDuser).child("userBMI").setValue(bmi)
                .addOnSuccessListener {
                    Toast.makeText(this, "BMI berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan BMI: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "BMI tidak tersedia untuk disimpan.", Toast.LENGTH_SHORT).show()
    }

    // fungsi untuk menyimpan hasil BMR, TDEE, Adjusted TDEE, dan Macronutrient ke targetGiziHarian lalu ke database
    private fun saveTargetGiziHarianToDatabase() {
        if (adjustedTdeeResult != null && macronutrientsResult != null) {
            val updates = mapOf(
                "targetKalori" to adjustedTdeeResult!!,
                "targetKarbohidrat" to (macronutrientsResult!!["carbs"] ?: 0.0),
                "targetProtein" to (macronutrientsResult!!["protein"] ?: 0.0),
                "targetLemak" to (macronutrientsResult!!["fat"] ?: 0.0)
            )

            dbTargetGiziHarian.child(iDuser).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Target gizi harian berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui target gizi harian: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Data Target Gizi tidak lengkap untuk diperbarui.", Toast.LENGTH_SHORT).show()
        }
    }


    // fungsi untuk menyimpan hasil Water Intake ke targetKonsumsiAir lalu ke database
    private fun saveTargetKonsumsiAirToDatabase() {
        waterIntakeResult?.let { waterIntake ->
            val updates = mapOf("targetKonsumsiAir" to waterIntake)

            dbTargetKonsumsiAir.child(iDuser).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Target konsumsi air berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui target konsumsi air: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "Konsumsi air tidak tersedia untuk diperbarui.", Toast.LENGTH_SHORT).show()
    }


    // Fungsi untuk menampilkan BMI dari database user
    private fun showUserBMI() {
        dbUser.child(iDuser).child("userBMI").addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get BMI value from database
                    val savedBMI = snapshot.getValue(Double::class.java)
                    if (savedBMI != null) {
                        hasilBMI.text = String.format("BMI Anda: %.1f", savedBMI) // Display with 1 decimal
                    } else {
                        hasilBMI.text = "BMI belum tersedia" // Display fallback message
                    }
                } else {
                    Toast.makeText(this@ResultActivity, "BMI belum ditemukan di database.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResultActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}