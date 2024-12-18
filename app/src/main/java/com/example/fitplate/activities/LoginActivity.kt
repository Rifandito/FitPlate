package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.AuthManager

import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.example.fitplate.R
import com.example.fitplate.RealtimeDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var loginInputUsername: EditText
    private lateinit var loginInputPassword: EditText
    private lateinit var buttonForgotPassword: Button
    private lateinit var buttonLogin: Button
    private lateinit var buttonCreateAkun: Button

    private val db by lazy { RealtimeDatabase.instance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)

        // Initialize AuthManager
        authManager = AuthManager(this)

        inisialisasiView()

        buttonLogin.setOnClickListener {
            val username = loginInputUsername.text.toString().trim()
            val password = loginInputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                authenticateUser(username, password)
            }
        }

        buttonCreateAkun.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // inisialisasi view
    private fun inisialisasiView(){
        loginInputUsername = findViewById(R.id.editTextUsername)
        loginInputPassword = findViewById(R.id.editTextPassword)
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonCreateAkun = findViewById(R.id.create)
    }

    // Authenticate user credentials
    private fun authenticateUser(username: String, password: String) {
        db.getReference().child("users").orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val storedPassword = userSnapshot.child("password").value.toString()
                            val userId = userSnapshot.child("idUser").value.toString()

                            // Match passwords (for simplicity; use hashing in production)
                            if (storedPassword == password) {
                                authManager.login(userId) // Use AuthManager to save login state
                                checkAdditionalData(userId)
                                return
                            }
                        }
                        Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "User not found. please create new account!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Check if the user has already submitted their additional data
    private fun checkAdditionalData(userId: String) {
        db.getReference().child("users").child(userId).child("usia")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Additional data exists, navigate to HomeActivity
                        navigateToHome()
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Additional data does not exist, navigate to InputUserActivity
                        navigateToInputUser()
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Error checking data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() //close login activity
    }

    private fun navigateToInputUser() {
        val intent = Intent(this, InputUserActivity::class.java)
        startActivity(intent)
        finish() //close login activity
    }
}