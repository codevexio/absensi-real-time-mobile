package com.example.absen.model

data class DetailPresensiResponse(
    val karyawan_id: Int,
    val nama_karyawan: String,
    val bulan: String,
    val rekap: List<DetailPresensi>
)
