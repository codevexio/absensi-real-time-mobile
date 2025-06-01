package com.example.absen.model

data class RiwayatDetailResponse(
    val nama_pengaju: String,
    val jenis_cuti: String,
    val jumlah_cuti_diambil: String,
    val status_approval: List<ApprovalStatus>,
    val alasan_penolakan: String?
)