package com.example.fitplate.dataclasses

data class TargetGiziHarian(
    val idTargetGiziHarian: String,
    val idUser: String,
    var targetKalori: Double,
    var targetKarbohidrat: Double,
    var targetProtein: Double,
    var targetLemak: Double
)