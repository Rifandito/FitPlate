package com.example.fitplate.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import android.widget.Toast

import com.example.fitplate.AuthManager
import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.example.fitplate.calculators.BadanProgressTracker
import com.example.fitplate.calculators.GiziProgressTracker
import com.example.fitplate.calculators.PointCalculator
import com.example.fitplate.databinding.HomeActivityBinding
import java.text.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeActivityBinding

    private val dbTargetGizi by lazy { RealtimeDatabase.instance().getReference("TargetGiziHarian") }

    private lateinit var authManager: AuthManager

    // variabel user dari database

    // variabel targetGizi dari database
    private var targetCalorie: Double? = null
    private var targetProtein: Double? = null
    private var targetKarbo: Double? = null
    private var targetLemak: Double? = null

    private val decimalFormat = DecimalFormat("#.0") // Satu angka di belakang koma

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // Inisialisasi binding
        binding = HomeActivityBinding.inflate(layoutInflater)
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

        loadData(userId)

        // Atur listener
        setupClickListeners()
    }

    // fungsi untuk menampilkan data
    private var isGiziTargetLoaded = false
    private var isGiziProgressLoaded = false
    private var isBadanProgressLoaded = false

    private var fetchedBadanProgressData: Map<String, Any?> = mapOf()
    private var fetchedGiziProgressData: Map<String, Double> = mapOf()

    private fun loadData(userId: String) {
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
                Toast.makeText(this@HomeActivity, "Datamu tidak ketemu", Toast.LENGTH_SHORT).show()
            }
            isGiziTargetLoaded = true
            checkDataLoaded()
        }

        val badanProgressTracker = BadanProgressTracker()
        badanProgressTracker.fetchUserData(userId, object : BadanProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Any?>) {
                fetchedBadanProgressData = data
                isBadanProgressLoaded = true
                checkDataLoaded()
            }
            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        val giziProgressTracker = GiziProgressTracker()
        giziProgressTracker.fetchDailyProgress(userId, object : GiziProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Double>) {
                fetchedGiziProgressData = data
                isGiziProgressLoaded = true
                checkDataLoaded()
            }
            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkDataLoaded() {
        if (isGiziTargetLoaded && isGiziProgressLoaded) {
            updateNutritionUI(fetchedGiziProgressData)
            checkAndAwardMedals(fetchedGiziProgressData)
        } else if (isBadanProgressLoaded && fetchedBadanProgressData.isNotEmpty()) {
            updateBbPreview(fetchedBadanProgressData)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNutritionUI(data: Map<String, Double>) {
        binding.apply {
//            textViewKaloriTarget.text = "${decimalFormat.format(progressCalorie ?: 0.0)}/${decimalFormat.format(targetCalorie ?: 0.0)} Kkal"
//            textViewProteinTarget.text = "${decimalFormat.format(progressProtein ?: 0.0)}/${decimalFormat.format(targetProtein ?: 0.0)} Kkal"
//            textViewKarboTarget.text = "${decimalFormat.format(progressKarbo ?: 0.0)}/${decimalFormat.format(targetKarbo ?: 0.0)} Kkal"
//            textViewLemakTarget.text = "${decimalFormat.format(progressLemak ?: 0.0)}/${decimalFormat.format(targetLemak ?: 0.0)} Kkal"

            textViewKaloriTarget.text = "${decimalFormat.format(data["jumlahKalori"] ?: 0.0)}/${decimalFormat.format(targetCalorie ?: 0.0)} Kkal"
            textViewProteinTarget.text = "${decimalFormat.format(data["jumlahProtein"] ?: 0.0)}/${decimalFormat.format(targetProtein ?: 0.0)} gram"
            textViewKarboTarget.text = "${decimalFormat.format(data["jumlahKarbohidrat"] ?: 0.0)}/${decimalFormat.format(targetKarbo ?: 0.0)} gram"
            textViewLemakTarget.text = "${decimalFormat.format(data["jumlahLemak"] ?: 0.0)}/${decimalFormat.format(targetLemak ?: 0.0)} gram"

//            progressBarKalori.progress = calculateProgress(progressCalorie, targetCalorie)
//            progressBarProtein.progress = calculateProgress(progressProtein, targetProtein)
//            progressBarKarbo.progress = calculateProgress(progressKarbo, targetKarbo)
//            progressBarLemak.progress = calculateProgress(progressLemak, targetLemak)

            progressBarKalori.progress = calculateProgress(data["jumlahKalori"], targetCalorie)
            progressBarProtein.progress = calculateProgress(data["jumlahProtein"], targetProtein)
            progressBarKarbo.progress = calculateProgress(data["jumlahKarbohidrat"], targetKarbo)
            progressBarLemak.progress = calculateProgress(data["jumlahLemak"], targetLemak)
        }
    }

    private fun updateBbPreview(data: Map<String, Any?>) {
        val weight = data["beratBadan"] ?: 0.0
        val targetWeight = data["targetBb"] ?: 0.0
        Log.d("UIUpdate", "Updating UI: weight = $weight, targetWeight = $targetWeight")
        binding.tvPreviewBb.text = "$weight kg/ $targetWeight kg"
    }

    private fun calculateProgress(progress: Double?, target: Double?): Int {
        return if (progress != null && target != null && target > 0) {
            ((progress / target) * 100).toInt()
        } else {
            0
        }
    }

    private fun calculateOverallProgress(data: Map<String, Double>): Int {
        val calorieProgress = calculateProgress(data["jumlahKalori"], targetCalorie)
        val proteinProgress = calculateProgress(data["jumlahProtein"], targetProtein)
        val carbProgress = calculateProgress(data["jumlahKarbohidrat"], targetKarbo)
        val fatProgress = calculateProgress(data["jumlahLemak"], targetLemak)

        // Calculate overall progress (each contributes 25%)
        val overallProgress = (
                (calorieProgress * 0.25) +
                        (proteinProgress * 0.25) +
                        (carbProgress * 0.25) +
                        (fatProgress * 0.25)
                ).toInt()
        return overallProgress
    }

    private fun checkAndAwardMedals(data: Map<String, Double>) {
        val userId = authManager.getUserId() ?: return
        val currentDate = getCurrentDateKey()

        // Reference to user's medal tracking
        val userMedalRef = RealtimeDatabase.instance().getReference("UserMedalTracking").child(userId)

        // Fetch last award data
        userMedalRef.get().addOnSuccessListener { snapshot ->
            val lastAwardDate = snapshot.child("lastAwardDate").value?.toString()
            val highestProgress = snapshot.child("highestProgress").value?.toString()?.toDoubleOrNull() ?: 0.0

            // Calculate overall progress
            val overallProgress = calculateOverallProgress(data).toDouble()

            if (lastAwardDate == currentDate && overallProgress <= highestProgress) {
                // No new progress to reward
                Log.d("HomeActivity", "No new progress to reward. Highest progress: $highestProgress%")
                return@addOnSuccessListener
            } else {
                // Log new progress
                Log.d("HomeActivity", "New Progress: $overallProgress%, Previous Highest: $highestProgress%")

                // Award medals for the new progress
                val pointCalculator = PointCalculator()
                pointCalculator.calculateAndAwardMedals(
                    userId = userId,
                    progress = overallProgress,
                    onSuccess = { message ->
                        Log.d("HomeActivity", message)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        // Update highest progress and last award date
                        userMedalRef.child("highestProgress").setValue(overallProgress)
                        userMedalRef.child("lastAwardDate").setValue(currentDate)
                    },
                    onFailure = { error ->
                        Log.e("HomeActivity", error)
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }.addOnFailureListener { error ->
            Log.e("HomeActivity", "Failed to fetch user medal tracking: ${error.message}")
            Toast.makeText(this, "Error checking medal status.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getCurrentDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun setupClickListeners() {

        binding.cardViewDietRecipe.setOnClickListener{
            navigateToActivity(RecipeActivity::class.java)
        }

        binding.cardViewMengukurTubuh.setOnClickListener {
            navigateToActivity(MengukurTubuhActivity::class.java)
        }

        binding.cardViewFoodTracker.setOnClickListener {
            navigateToActivity(FoodJournalActivity::class.java)
        }

        binding.profileButton.setOnClickListener {
            navigateToActivity(ProfilActivity::class.java)
        }

        binding.cardViewMedalGuide.setOnClickListener {
            navigateToActivity(GuideActivity::class.java)
        }

        binding.leaderboardButton.setOnClickListener {
            navigateToActivity(LeaderboardActivity::class.java)
        }
    }

    private fun <T> navigateToActivity(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}
