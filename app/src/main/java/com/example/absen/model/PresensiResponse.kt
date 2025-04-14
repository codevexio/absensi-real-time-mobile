package com.example.absen.model

data class PresensiResponse(
    val status: String,
    val message: String,
    val data: PresensiData?
)
