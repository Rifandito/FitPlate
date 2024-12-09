package com.example.fitplate.dataclasses

data class Resep(
    val idResep: String,
    var namaResep: String,
    var gambar: String,
    var deskripsi: String,
    var totalKalori: Double
)