package com.example.absen.model

import com.google.gson.annotations.SerializedName

data class CekPresensiResponse(
    @SerializedName("bisaPresensiMasuk") val bisaPresensiMasuk: Boolean,
    @SerializedName("bisaPresensiPulang") val bisaPresensiPulang: Boolean,
    @SerializedName("message") val message: String
)