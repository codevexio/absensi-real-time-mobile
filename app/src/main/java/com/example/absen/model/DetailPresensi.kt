package com.example.absen.model

data class DetailPresensi(
    val tanggal: String,
    val jam_masuk: String?,
    val status_masuk: String?,
    val jam_pulang: String?,
    val status_pulang: String?,
    val shift: String?
)
