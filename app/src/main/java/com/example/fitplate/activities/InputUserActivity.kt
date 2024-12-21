package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager

import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class InputUserActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var usiaInput: EditText
    private lateinit var tujuanDietInput: MaterialAutoCompleteTextView
    private lateinit var olahragaInput: MaterialAutoCompleteTextView
    private lateinit var genderInput: MaterialAutoCompleteTextView
    private lateinit var tingkatKesulitanInput: MaterialAutoCompleteTextView
    private lateinit var airInput: EditText
    private lateinit var makanInput: EditText
    private lateinit var tinggiInput: EditText
    private lateinit var beratInput: EditText
    private lateinit var targetInput: EditText
    private lateinit var tombolSimpan: Button

    private val db by lazy { RealtimeDatabase.instance().getReference() }
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.input_user_activity)

        // Initialize AuthManager
        authManager = AuthManager(this)

        // Get user ID from AuthManager
        userId = authManager.getUserId() ?: run {
            Toast.makeText(this, "Error: User not logged in!", Toast.LENGTH_SHORT).show()
            finish() // Close activity if user is not logged in
            return
        }

        initializeViews()
        initializeDropdowns()

        tombolSimpan.setOnClickListener {
            saveUserData()
            navigateToResult()
        }
    }

    // inisialisasi view
    private fun initializeViews() {
        usiaInput = findViewById(R.id.editTextUsia)
        tujuanDietInput = findViewById(R.id.dropdownTujuanDiet)
        olahragaInput = findViewById(R.id.dropdownOlahraga)
        genderInput = findViewById(R.id.dropdownGender)
        tingkatKesulitanInput = findViewById(R.id.dropdownTingkatan)
        airInput = findViewById(R.id.editTextAir)
        makanInput = findViewById(R.id.editTextMakan)
        tinggiInput = findViewById(R.id.editTextTinggi)
        beratInput = findViewById(R.id.editTextBerat)
        targetInput = findViewById(R.id.editTextTarget)
        tombolSimpan = findViewById(R.id.buttonDapatkan)
    }

    // inisialisasi dropdown
    private fun initializeDropdowns() {
        // Define dropdown data
        val tujuanDietList = listOf("Turun Berat Badan", "Tambah Berat Badan", "Jaga Berat Badan")
        val olahragaList = listOf("Tidak Pernah", "1-2 hari/minggu", "3-5 hari/minggu", "6-7 hari/minggu", "Olahraga Berat")
        val genderList = listOf("Pria", "Wanita")
        val tingkatKesulitanList = listOf("Beginner", "Intermediate", "Advanced")

        // Initialize dropdown views
        tujuanDietInput = findViewById(R.id.dropdownTujuanDiet)
        olahragaInput = findViewById(R.id.dropdownOlahraga)
        genderInput = findViewById(R.id.dropdownGender)
        tingkatKesulitanInput = findViewById(R.id.dropdownTingkatan)

        // Set adapters
        tujuanDietInput.setAdapter(createAdapter(tujuanDietList))
        olahragaInput.setAdapter(createAdapter(olahragaList))
        genderInput.setAdapter(createAdapter(genderList))
        tingkatKesulitanInput.setAdapter(createAdapter(tingkatKesulitanList))
    }

    private fun createAdapter(data: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, data)
    }

    private fun saveUserData() {
        // Collect user input
        val usia = usiaInput.text.toString().toIntOrNull()
        val tujuanDiet = tujuanDietInput.text.toString()
        val olahraga = olahragaInput.text.toString()
        val gender = genderInput.text.toString()
        val tingkatKesulitan = tingkatKesulitanInput.text.toString()
        val air = airInput.text.toString().toDoubleOrNull()
        val makan = makanInput.text.toString().toIntOrNull()
        val tinggi = tinggiInput.text.toString().toDoubleOrNull()
        val berat = beratInput.text.toString().toDoubleOrNull()
        val target = targetInput.text.toString().toDoubleOrNull()

        // Validate required fields
        if (usia == null || tinggi == null || berat == null || target == null) {
            Toast.makeText(this, "tolong isi semua kotak isian", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare updates for Firebase
        val updates = mutableMapOf<String, Any?>(
            "usia" to usia,
            "tujuanDiet" to tujuanDiet,
            "intensitasOlahraga" to olahraga,
            "jenisKelamin" to gender,
            "tingkatKesulitan" to tingkatKesulitan,
            "konsumsiAirUser" to air,
            "frekuensiMakan" to makan,
            "tinggiBadan" to tinggi,
            "beratBadan" to berat,
            "targetBb" to target
        )

        // Update Firebase database
        db.child("users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                //Toast.makeText(this, "Data successfully updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "gagal mengupdate data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        finish()
    }
}