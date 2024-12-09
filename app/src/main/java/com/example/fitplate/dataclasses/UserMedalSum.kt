package com.example.fitplate.dataclasses

data class UserMedalSum(
    val idMedalSum: String,
    val idUser: String,
    var bronzeCount: Int,
    var silverCount: Int,
    var goldCount: Int
)