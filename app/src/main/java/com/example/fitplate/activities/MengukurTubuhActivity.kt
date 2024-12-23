package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager
import com.example.fitplate.R
import com.example.fitplate.calculators.BadanProgressTracker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MengukurTubuhActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    private lateinit var heightValue: TextView
    private lateinit var weightValue: TextView
    private lateinit var targetValue: TextView
    private lateinit var newWeightInput: EditText
    private lateinit var newHeightInput: EditText
    private lateinit var updateButton: Button
    private lateinit var mengukurToHome: ImageButton

    private var currentDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_bb_activity)

        authManager = AuthManager(this)
        val userId = authManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        inisialisasiView()

        fetchProgressBadan(userId)

        mengukurToHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        updateButton.setOnClickListener {
            updateUserProgress(userId)
        }
    }

    private fun inisialisasiView(){
        heightValue = findViewById(R.id.heightValue)
        weightValue = findViewById(R.id.weightValue)
        targetValue = findViewById(R.id.targetValue)
        newHeightInput = findViewById(R.id.newHeightInput)
        newWeightInput = findViewById(R.id.newWeightInput)
        updateButton = findViewById(R.id.updateButton)
        mengukurToHome = findViewById(R.id.mengukurToHome)
    }

    private var isBadanProgressLoaded = false
    private var fetchedProgressData: Map<String, Double> = mapOf()
    private fun fetchProgressBadan(userId: String){
        val progressTracker = BadanProgressTracker()
        progressTracker.fetchUserData(userId, object : BadanProgressTracker.ProgressCallback {
            override fun onSuccess(data: Map<String, Double>) {
                fetchedProgressData = data
                isBadanProgressLoaded = true
                checkDataLoaded()
            }
            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@MengukurTubuhActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUpdatedData(userId: String): Map<String, Double> {
        val newTinggi = newHeightInput.text.toString().toDoubleOrNull() ?: 0.0
        val newBerat = newWeightInput.text.toString().toDoubleOrNull() ?: 0.0

        return mapOf(
            "beratBadan" to newBerat,
            "tinggiBadan" to newTinggi
        )
    }

    private fun updateUserProgress(userId: String) {
        val updatedData = getUpdatedData(userId)
        val progressTracker = BadanProgressTracker()

        progressTracker.updateUserData(userId, updatedData, object : BadanProgressTracker.UpdateCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(this@MengukurTubuhActivity, message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(errorMessage: String) {
                Toast.makeText(this@MengukurTubuhActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun checkDataLoaded() {
        if (isBadanProgressLoaded) {
            updateNutritionUI(fetchedProgressData)
        }
    }

    private fun updateNutritionUI(data: Map<String, Double>) {
        val height = data["tinggiBadan"] ?: 0.0
        val weight = data["beratBadan"] ?: 0.0
        val targetWeight = data["targetBb"] ?: 0.0
        heightValue.text = "${height} cm"
        weightValue.text = "${weight} kg"
        targetValue.text = "${targetWeight} kg"
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
