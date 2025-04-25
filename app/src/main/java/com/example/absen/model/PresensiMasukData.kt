package com.example.absen.model

data class PresensiMasukData(
    val karyawan_id: Int,
    val tanggalPresensi: String,
    val waktuMasuk: String,
    val statusMasuk: String,
    val imageMasuk: String, // path image yang disimpan
    val lokasiMasuk: String // lokasi dalam bentuk json
)