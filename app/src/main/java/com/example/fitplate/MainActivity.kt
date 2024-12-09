package com.example.fitplate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitplate.dataclasses.*

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var etJenisMedali: EditText
    private lateinit var etPoinBawaan: EditText
    private lateinit var bSave: Button

    private val db by lazy { RealtimeDatabase.instance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        inisialisasiView()

        bSave.apply {
            setOnClickListener{
                setDataToDatabase(getDataPengguna())
            }
        }
    }

    private fun inisialisasiView(){
        etJenisMedali = findViewById(R.id.etJenismedali)
        etPoinBawaan = findViewById(R.id.etPoinBawaan)
        bSave = findViewById(R.id.bSave)
    }

    // ngambil data pengguna dari input
    private fun getDataPengguna(): Medali {
        val jenisMedali = etJenisMedali.text.toString()
        val poinBawaanText = etPoinBawaan.text.toString()
        val poinBawaan = poinBawaanText.toIntOrNull() ?: 0
        return Medali(
            idMedali = UUID.randomUUID().toString(),
            jenisMedali = jenisMedali,
            poinBawaan = poinBawaan
        )
    }

    // write data ke database
    private fun setDataToDatabase(medali: Medali){
        if (medali.jenisMedali.isNotEmpty() && medali.poinBawaan > 0){
            bSave.isEnabled = false

            db.getReference("medali").child(medali.idMedali).setValue(medali).addOnSuccessListener {
                etJenisMedali.setText("")
                etPoinBawaan.setText("")
                bSave.isEnabled = true

                Toast.makeText(this, "Data berhasil tersimpan", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                bSave.isEnabled = true
                Toast.makeText(this, "Data gagal tersimpan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}