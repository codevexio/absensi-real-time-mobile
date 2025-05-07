package com.example.absen.model

import com.google.gson.annotations.SerializedName

data class StatistikKehadiranResponse(
    @SerializedName("jumlah_terlambat") val jumlahTerlambat: Int,
    @SerializedName("jumlah_tepat_waktu") val jumlahTepatWaktu: Int
)
