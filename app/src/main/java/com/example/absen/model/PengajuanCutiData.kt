package com.example.absen.model

data class PengajuanCutiData(
    val id: Int,
    val jenisCuti: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val jumlahHari: Int,
    val statusCuti: String,
    val file_surat_cuti: String?
)