package com.example.absen.model

data class DetailPengajuanCutiResponse(
    val nama_karyawan: String,
    val tanggal_pengajuan: String,
    val tanggal_mulai: String,
    val tanggal_selesai: String,
    val file_surat_cuti: String?
)


