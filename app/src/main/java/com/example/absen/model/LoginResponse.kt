package com.example.absen.model

data class LoginResponse(
    val message: String,
    val token: String,
    val karyawan: KaryawanData
)