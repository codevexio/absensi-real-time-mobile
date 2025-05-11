package com.example.absen.model

import com.google.gson.annotations.SerializedName


data class SisaCutiData(
    @SerializedName("cuti_tahunan") val cutiTahunan: Int,
    @SerializedName("cuti_panjang") val cutiPanjang: Int
)