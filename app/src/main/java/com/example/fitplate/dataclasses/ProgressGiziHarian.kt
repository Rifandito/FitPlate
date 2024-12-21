package com.example.fitplate.dataclasses
import java.util.*

data class ProgressGiziHarian (
    val idProgressGiziHarian: String,
    val idUser: String,
    var tanggal: String,
    var jumlahKalori: Double,
    var jumlahKarbohidrat: Double,
    var jumlahProtein: Double,
    var jumlahLemak: Double,
    var status: String? = null
)