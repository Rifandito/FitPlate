package com.example.fitplate.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.R

class ArtikelAirActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.artikel_air_activity)

        fun onBackPressed() {
            super.onBackPressed() // Menyelesaikan aktivitas ini dan kembali ke layar sebelumnya
        }

    }
}
