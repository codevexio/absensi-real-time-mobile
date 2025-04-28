package com.example.absen.api

import com.example.absen.model.CekWaktuPresensiResponse
import com.example.absen.model.LoginRequest
import com.example.absen.model.LoginResponse
import com.example.absen.model.LokasiMasuk
import com.example.absen.model.PresensiMasukResponse
import com.example.absen.model.PresensiPulangResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("cek-waktu-presensi")
    fun cekWaktuPresensi(): Call<CekWaktuPresensiResponse> // Tanpa parameter token lagi

    @Multipart
    @POST("presensi/masuk")
    suspend fun presensiMasuk(
        @Part imageMasuk: MultipartBody.Part,
        @Part("lokasiMasuk[latitude]") latitude: RequestBody,
        @Part("lokasiMasuk[longitude]") longitude: RequestBody
    ): Response<PresensiMasukResponse>

    @Multipart
    @POST("presensi/pulang")
    suspend fun presensiPulang(
        @Part imagePulang: MultipartBody.Part,
        @Part("lokasiPulang[latitude]") latitude: RequestBody,
        @Part("lokasiPulang[longitude]") longitude: RequestBody
    ): Response<PresensiPulangResponse>
}