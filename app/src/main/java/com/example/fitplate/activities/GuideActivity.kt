package com.example.fitplate.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.R

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guide_activity) // Replace with your actual layout file

        fun onBackPressed() {
            super.onBackPressed() // Menyelesaikan aktivitas ini dan kembali ke layar sebelumnya
        }
    }


}