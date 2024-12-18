package com.example.fitplate.dataclasses

data class User(
    val idUser: String,
    var username: String,
    var password: String,
    var namaLengkap: String,
    var konsumsiAirUser: Double? = null,
    var beratBadan: Double? = null,
    var tinggiBadan: Double? = null,
    var usia: Int? = null,
    var jenisKelamin: String? = null,
    var targetBb: Double? = null,
    var tujuanDiet: String? = null,
    var intensitasOlahraga: String? = null,
    var frekuensiMakan: Int? = null,
    var userBMI: Double? = null
)