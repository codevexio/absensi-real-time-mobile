package com.example.absen.api

import com.example.absen.model.LoginRequest
import com.example.absen.model.LoginResponse
import com.example.absen.model.PresensiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("api/presensi-masuk")
    suspend fun presensiMasuk(
        @Header("Authorization") token: String,
        @Part("jadwal_kerja_id") jadwalKerjaId: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<PresensiResponse>

    @Multipart
    @POST("api/presensi-pulang")
    suspend fun presensiPulang(
        @Header("Authorization") token: String,
        @Part("jadwal_kerja_id") jadwalKerjaId: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<PresensiResponse>
}