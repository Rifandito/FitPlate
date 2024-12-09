package com.example.fitplate.dataclasses
import java.util.*

data class KonsumsiAir(
    val idKonsumsi: String,
    val idUser: String,
    var tanggal: Date,
    var jumlahAir: Int,
    var status: String
)
