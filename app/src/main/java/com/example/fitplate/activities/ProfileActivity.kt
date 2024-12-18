package com.example.fitplate.activities

import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.RealtimeDatabase

class ProfileActivity : AppCompatActivity() {

    private val db by lazy { RealtimeDatabase.instance().getReference() }
}