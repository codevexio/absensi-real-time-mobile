package com.example.absen.model

data class PresensiData(
    val id: Int,
    val karyawan_id: Int,
    val jadwal_kerja_id: Int,
    val waktu: String,
    val status: String,
    val lokasi_lat: String,
    val lokasi_lng: String,
    val foto: String
)