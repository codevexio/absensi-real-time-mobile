package com.example.absen.model

data class PresensiMasukData(
    val id: Int,
    val karyawan_id: Int,
    val tanggalPresensi: String,
    val waktuMasuk: String,
    val statusMasuk: String,
    val imageMasuk: String,
    val lokasiMasuk: String,
    val jadwal_kerja_id: Int // lokasi dalam bentuk json
)