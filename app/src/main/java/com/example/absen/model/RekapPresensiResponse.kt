package com.example.absen.model

data class RekapPresensiResponse(
    val karyawan_id: Int,
    val nama_karyawan: String,
    val rekap_presensi: List<RekapPresensi>
)