package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.calculators.BadanProgressTracker
import com.example.fitplate.calculators.PointProgressTracker
import com.example.fitplate.databinding.ProfilActivityBinding
import java.text.DecimalFormat

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ProfilActivityBinding
    private lateinit var authManager: AuthManager

    private val decimalFormat = DecimalFormat("#.0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding
        binding = ProfilActivityBinding.inflate(layoutInflater)
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

        fetchProgressBadan(userId)
        fetchUserSkorandMedal(userId)

        binding.profilToHome.setOnClickListener {
            finish()
        }

        setupLogoutButton()
    }

    private var isBadanProgressLoaded = false
    private var fetchedProgressData: Map<String, Any?> = mapOf()
    private fun fetchProgressBadan(userId: String){
        val progressTracker = BadanProgressTracker()
        progressTracker.fetchUserData(userId, object : BadanProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Any?>) {
                fetchedProgressData = data
                isBadanProgressLoaded = true
                checkDataLoaded()
            }
            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@ProfilActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserSkorandMedal(userId: String) {
        val pointProgressTracker = PointProgressTracker()

        // Fetch user score
        pointProgressTracker.fetchSkorData(userId, object : PointProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Any?>) {
                val skor = data["skor"] as? Int ?: 0
                binding.tvTotalPoin.text = "$skor"
            }

            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@ProfilActivity, "Error fetching score: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch user medal counts
        pointProgressTracker.fetchUserMedalData(userId, object : PointProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Any?>) {
                val goldCount = data["goldCount"] as? Int ?: 0
                val silverCount = data["silverCount"] as? Int ?: 0
                val bronzeCount = data["bronzeCount"] as? Int ?: 0
                binding.totalGold.text = "$goldCount"
                binding.totalPerak.text = "$silverCount"
                binding.totalPerunggu.text = "$bronzeCount"
            }

            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@ProfilActivity, "Error fetching medals: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun checkDataLoaded() {
        if (isBadanProgressLoaded) {
            updateNutritionUI(fetchedProgressData)
        }
    }

    private fun updateNutritionUI(data: Map<String, Any?>) {
        val height = data["tinggiBadan"] ?: 0.0
        val weight = data["beratBadan"] ?: 0.0
        val targetWeight = data["targetBb"] ?: 0.0
        val userBmi = data["userBMI"] ?: 0.0
        val nama = data["nama"]
        binding.tinggiBadanProfil.text = "${height} cm"
        binding.BeratBadanProfil.text = "${weight} kg"
        binding.targetBBprofil.text = "${targetWeight} kg"
        binding.bmiProfil.text = "BMI saat ini = ${decimalFormat.format(userBmi)}"
        binding.profileName.text = "$nama"

        teller(userBmi)
    }

    private fun teller(bmi: Any){
        if (bmi as Double > 40.0) {
            binding.penjelasBMI.text = "Kamu sudah termasuk obesitas tingkat kelas 3"
        } else if (bmi in 35.0..39.9) {
            binding.penjelasBMI.text = "Kamu sudah termasuk obesitas tingkat kelas 2"
        } else if (bmi in 30.0..34.9) {
            binding.penjelasBMI.text = "Kamu sudah termasuk obesitas tingkat kelas 1"
        } else if (bmi in 25.0..29.9) {
            binding.penjelasBMI.text = "Kamu sudah termasuk overweight"
        } else if (bmi in 18.5..24.9) {
            binding.penjelasBMI.text = "Berat badan kamu normal"
        } else if (bmi < 18.5) {
            binding.penjelasBMI.text = "Kamu sudah termasuk underweight"
        } else {
            binding.penjelasBMI.text = "berat badan tidak ditemukan"
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            // Clear user session using AuthManager
            val authManager = AuthManager(this)
            authManager.logout()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

}
