package com.example.absen.model

import com.google.gson.annotations.SerializedName

data class SisaCutiResponse(
    @SerializedName("sisaCutiTahun")
    val sisaCutiTahun: Int? = 0,

    @SerializedName("sisaCutiPanjang")
    val sisaCutiPanjang: Int? = 0
)
