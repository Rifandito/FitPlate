package com.example.fitplate.dataclasses
import java.util.*

data class ProgressKonsumsiAir(
    val idProgressKonsumsiAir: String,
    val idUser: String,
    var tanggal: Date,
    var jumlahAir: Double,
    var status: String? = null
)