package com.example.fitplate.dataclasses

data class User(
    val idUser: String,
    var username: String? = null,
    var password: String? = null,
    var namaLengkap: String? = null,
    var beratBadan: Double? = null,
    var tinggiBadan: Double? = null,
    var usia: Int? = null,
    var jenisKelamin: String? = null,
    var targetBb: Double? = null,
    var tujuanDiet: String? = null,
    var intensitasOlahraga: String? = null,
    var frekuensiMakan: Int? = null,
    var kebutuhanMinimumKalori: Double? = null
)