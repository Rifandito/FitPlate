package com.example.fitplate.dataclasses

data class Skor(
    val idLevel: String,
    val idUser: String,
    var level: Int,
    var totalPoints: Int
)