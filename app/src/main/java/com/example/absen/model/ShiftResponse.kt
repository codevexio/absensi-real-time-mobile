package com.example.absen.model

data class ShiftResponse(
    val status: Boolean,
    val message: String,
    val shift: ShiftData?
)