package com.example.fitplate.dataclasses
import java.util.*

data class Makanan(
    val idMakanan: String,
    val idUser: String,
    var tanggal: Date,
    var namaMakanan: String,
    var karbohidrat: Double,
    var protein: Double,
    var lemak: Double,
    var kalori: Double
)