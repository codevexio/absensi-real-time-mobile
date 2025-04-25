package com.example.absen.model

import okhttp3.MultipartBody

data class PresensiMasukRequest(
    val imageMasuk: MultipartBody.Part,
    val lokasiMasuk: LokasiMasuk
)