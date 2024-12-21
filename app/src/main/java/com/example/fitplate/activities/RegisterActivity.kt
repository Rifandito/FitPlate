package com.example.fitplate.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.dataclasses.*
import java.util.*
import java.text.SimpleDateFormat

import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var tombolRegister: Button

    private val db by lazy { RealtimeDatabase.instance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_activity)

        inisialisasiView()

        tombolRegister.setOnClickListener {
            val user = getInputData()
            if (validateInput(user)) {
                checkUsernameUniqueness(user)
            }
        }
    }

    // inisialisasi semua view
    private fun inisialisasiView() {
        nameInput = findViewById(R.id.editTexName)
        usernameInput = findViewById(R.id.editTextUsername)
        passwordInput = findViewById(R.id.editTextPassword)
        tombolRegister = findViewById(R.id.buttonRegister)
    }

    //fungsi untuk ngambil data dari input, kemudian dimasukkan ke objek
    private fun getInputData(): User {
        // ambil data dari input
        val name = nameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // masukkan ke objek
        return User(
            idUser = UUID.randomUUID().toString(),
            namaLengkap = name,
            username = username,
            password = password
        )
    }

    // Validate input data
    private fun validateInput(user: User): Boolean {
        if (user.namaLengkap.isEmpty() || user.username.isEmpty() || user.password.isEmpty()) {
            Toast.makeText(this, "Tolong isi semua data anda", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (user.password.length < 6) {
            Toast.makeText(this, "Password harus berisi setidaknya 6 karakter!", Toast.LENGTH_SHORT).show()
            return false
        }
        else {
            return true
        }
    }

    // Check if the username is already taken
    private fun checkUsernameUniqueness(user: User) {
        db.getReference().child("users").orderByChild("username").equalTo(user.username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegisterActivity, "Username already exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        saveDataToDatabase(user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // fungsi untuk simpan data ke database
    private fun saveDataToDatabase(user: User) {
        tombolRegister.isEnabled = false

        val userRef = db.getReference().child("users").child(user.idUser)

        userRef.setValue(user)
            .addOnSuccessListener {
                createAssociatedData(user.idUser)
                saveUserIdToPreferences(user.idUser)
                clearInputFields()
                tombolRegister.isEnabled = true
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .addOnFailureListener {
                tombolRegister.isEnabled = true
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    // fungsi untuk membuat root setiap data class
    // yang berhubungan dengan user
    private fun createAssociatedData(userId: String) {
        val currentDate = getCurrentDate()
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Generate date key

        // Initialize continuous data
        val progressGiziHarian = ProgressGiziHarian(
            idProgressGiziHarian = "$userId-$dateKey",
            idUser = userId,
            tanggal = currentDate,
            jumlahKalori = 0.0,
            jumlahKarbohidrat = 0.0,
            jumlahProtein = 0.0,
            jumlahLemak = 0.0,
            status = null
        )
        val progressKonsumsiAir = ProgressKonsumsiAir(
            idProgressKonsumsiAir = "$userId-$dateKey",
            idUser = userId,
            tanggal = currentDate,
            jumlahAir = 0.0,
            status = null
        )

        // Initialize discrete data
        val targetGiziHarian = TargetGiziHarian(
            idTargetGiziHarian = UUID.randomUUID().toString(),
            idUser = userId,
            targetKalori = 2000.0,
            targetKarbohidrat = 300.0,
            targetProtein = 50.0,
            targetLemak = 70.0
        )
        val targetAirHarian = TargetKonsumsiAir(
            idTargetKonsumsiAir = UUID.randomUUID().toString(),
            idUser = userId,
            targetKonsumsiAir = 2.0
        )
        val skor = Skor(
            idLevel = UUID.randomUUID().toString(),
            idUser = userId,
            totalPoints = 0
        )
        val userMedalSum = UserMedalSum(
            idMedalSum = UUID.randomUUID().toString(),
            idUser = userId,
            bronzeCount = 0,
            silverCount = 0,
            goldCount = 0
        )

        // Save data to database
        val dbRef = db.getReference()

        // Continuous data saved under userId/dateKey
        dbRef.child("ProgressGiziHarian").child(userId).child(dateKey).setValue(progressGiziHarian)
        dbRef.child("ProgressKonsumsiAir").child(userId).child(dateKey).setValue(progressKonsumsiAir)

        // Discrete data saved under userId
        dbRef.child("TargetGiziHarian").child(userId).setValue(targetGiziHarian)
        dbRef.child("TargetKonsumsiAir").child(userId).setValue(targetAirHarian)
        dbRef.child("Skor").child(userId).setValue(skor)
        dbRef.child("UserMedalSum").child(userId).setValue(userMedalSum)
    }

    // save user id dalam sharedpreference
    private fun saveUserIdToPreferences(userId: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("USER_ID", userId)
            apply()
        }
    }

    // Clear input fields after registration
    private fun clearInputFields() {
        nameInput.text.clear()
        usernameInput.text.clear()
        passwordInput.text.clear()
    }

    // Navigate to LoginActivity
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}