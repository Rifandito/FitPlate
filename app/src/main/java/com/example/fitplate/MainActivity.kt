package com.example.fitplate

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitplate.activities.*

import android.widget.Button
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    private val db by lazy { RealtimeDatabase.instance().getReference("users") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        authManager = AuthManager(this)

        val buttonMain = findViewById<Button>(R.id.buttonMain)

        buttonMain.setOnClickListener {
            if (authManager.checkLoginStatus()) {
                val userId = authManager.getUserId() // Retrieve the logged-in user's ID
                if (userId != null) {
                    // Check if user ID exists in the database
                    verifyUserIdInDatabase(userId)
                } else {
                    Toast.makeText(this, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                navigateToLogin()
            }
        }
    }

    private fun verifyUserIdInDatabase(userId: String) {
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User exists in the database, check for additional data
                    checkUserAdditionalData(userId)
                } else {
                    // User does not exist in the database, clear SharedPreferences and navigate to login
                    authManager.logout()
                    Toast.makeText(this@MainActivity, "Session expired. Please log in again or create new account", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUserAdditionalData(userId: String) {
        // Check if additional data has been submitted in Firebase
        db.child(userId).child("usia").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Additional data exists, navigate to HomeActivity
                    navigateToHome()
                } else {
                    // Additional data does not exist, navigate to InputUserActivity
                    navigateToUserInput()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error checking user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToUserInput() {
        val intent = Intent(this, InputUserActivity::class.java)
        startActivity(intent)
        finish()
    }
}


//Rifandito Daniswara : 22523219
//Diena Mukafasyadiah : 22523269
//Mahendi Putri Dinanti : 21523001

// Username : Pablo
// Password : Escobar