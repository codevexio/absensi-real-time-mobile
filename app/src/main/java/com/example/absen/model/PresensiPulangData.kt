package com.example.absen.model

data class PresensiPulangData (
    val id: Int,
    val karyawan_id: Int,
    val tanggalPresensi: String,
    val waktuPulang: String,
    val statusPulang: String,
    val imagePulang: String,
    val lokasiPulang: String,
    val jadwal_kerja_id: Int
)