package com.example.absen.api

import com.example.absen.model.AjukanCutiResponse
import com.example.absen.model.ApiResponse
import com.example.absen.model.CekWaktuPresensiResponse
import com.example.absen.model.DetailPengajuanCutiResponse
import com.example.absen.model.DetailPresensiResponse
import com.example.absen.model.KeterlambatanData
import com.example.absen.model.ListPengajuanCutiResponse
import com.example.absen.model.LoginRequest
import com.example.absen.model.LoginResponse
import com.example.absen.model.PresensiMasukResponse
import com.example.absen.model.PresensiPulangResponse
import com.example.absen.model.RekapPresensiResponse
import com.example.absen.model.RiwayatDetailResponse
import com.example.absen.model.RiwayatPengajuanCuti
import com.example.absen.model.ShiftResponse
import com.example.absen.model.SisaCutiResponse
import com.example.absen.model.StatistikKehadiranResponse
import com.example.absen.model.StatusHariIni
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

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

    @GET("presensi/status-hari-ini")
    suspend fun cekStatusPresensi(): Response<StatusHariIni>

    @GET("list-rekap-presensi")
    suspend fun listRekapPresensi(): Response<RekapPresensiResponse>

    @GET("detail-rekap/{bulan}")
    suspend fun getDetailPresensi(
        @Path("bulan") bulan: String
    ): Response<DetailPresensiResponse>

    @GET("rekap-presensi-pdf/{bulan}")
    @Streaming
    suspend fun downloadRekapPdf(
        @Path("bulan") bulan: String
    ): Response<ResponseBody>

    @GET("cuti/riwayat")
    suspend fun getRiwayatPengajuan(): Response<List<RiwayatPengajuanCuti>>

    @GET("cuti/riwayat/{id}")
    suspend fun getDetailRiwayat(
        @Path("id") id: Int
    ): Response<RiwayatDetailResponse>

    @GET("keterlambatan/statistik-bulanan")
    suspend fun getStatistikKehadiran(): Response<StatistikKehadiranResponse>

    @GET("keterlambatan/daftar")
    suspend fun getDaftarTerlambat(): Response<List<KeterlambatanData>>

    // Pastikan bahwa kamu menggunakan token dengan benar
    @GET("shift/hari-ini")
    suspend fun getShiftHariIni(
        @Header("Authorization") token: String
    ): Response<ShiftResponse>

    @GET("cuti/sisa")
    suspend fun getSisaCuti(): Response<SisaCutiResponse>

    @Multipart
    @POST("cuti/ajukan")
    suspend fun ajukanCuti(
        @Part("tanggalMulai") tanggalMulai: RequestBody,
        @Part("tanggalSelesai") tanggalSelesai: RequestBody,
        @Part("jenisCuti") jenisCuti: RequestBody,
        @Part dokumen: MultipartBody.Part
    ): Response<AjukanCutiResponse>

    @GET("approval-cuti")
    suspend fun getPengajuanCuti(
        @Header("Authorization") token: String
    ): Response<ListPengajuanCutiResponse>

    @GET("approval-cuti/{id}")
    suspend fun getDetailPengajuanCuti(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DetailPengajuanCutiResponse>

    @FormUrlEncoded
    @POST("approval-cuti/{id}/proses")
    suspend fun postApprovalCuti(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("status") status: String,
        @Field("catatan") catatan: String?
    ): Response<ApiResponse>










}