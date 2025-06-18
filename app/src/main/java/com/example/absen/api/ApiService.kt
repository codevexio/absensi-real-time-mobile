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
    // Login Karyawan
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Melihat Status Presensi Untuk Hari Ini
    @GET("presensi/status-hari-ini")
    suspend fun cekStatusPresensi(): Response<StatusHariIni>

    // Melihat Waktu Presensi
    // cek bisaPresensiMasuk dan bisaPresensiPulang
    @GET("cek-waktu-presensi")
    fun cekWaktuPresensi(): Call<CekWaktuPresensiResponse>

    // POST Presensi Masuk
    @Multipart
    @POST("presensi/masuk")
    suspend fun presensiMasuk(
        @Part imageMasuk: MultipartBody.Part,
        @Part("lokasiMasuk[latitude]") latitude: RequestBody,
        @Part("lokasiMasuk[longitude]") longitude: RequestBody
    ): Response<PresensiMasukResponse>

    // POST Presensi Pulang
    @Multipart
    @POST("presensi/pulang")
    suspend fun presensiPulang(
        @Part imagePulang: MultipartBody.Part,
        @Part("lokasiPulang[latitude]") latitude: RequestBody,
        @Part("lokasiPulang[longitude]") longitude: RequestBody
    ): Response<PresensiPulangResponse>

    // Menampilkan Riwayat Presensi Berdasarkan Bulannya
    @GET("list-rekap-presensi")
    suspend fun listRekapPresensi(): Response<RekapPresensiResponse>

    // Menampilkan Detail Riwayat Presensi yang Dipilih
    @GET("detail-rekap/{bulan}")
    suspend fun getDetailPresensi(
        @Path("bulan") bulan: String
    ): Response<DetailPresensiResponse>

    // Download PDF Detail Riwayat Presensi yang Dipilih
    @GET("rekap-presensi-pdf/{bulan}")
    @Streaming
    suspend fun downloadRekapPdf(
        @Path("bulan") bulan: String
    ): Response<ResponseBody>

    // Menampilkan Riwayat Pengajuan Cuti Karyawan
    @GET("cuti/riwayat")
    suspend fun getRiwayatPengajuan(): Response<List<RiwayatPengajuanCuti>>

    // Menampilkan Detail Riwayat Pengajuan Cuti Karyawan yang Dipilih
    @GET("cuti/riwayat/{id}")
    suspend fun getDetailRiwayat(
        @Path("id") id: Int
    ): Response<RiwayatDetailResponse>

    // Menampilkan Statistik Kehadiran Karyawan Berdasarkan Bulan Saat Ini
    @GET("keterlambatan/statistik-bulanan")
    suspend fun getStatistikKehadiran(): Response<StatistikKehadiranResponse>

    // Menampilkan Karyawan Terlambat Hari Ini
    @GET("keterlambatan/daftar")
    suspend fun getDaftarTerlambat(): Response<List<KeterlambatanData>>

    // Menampilkan Shift Karyawan Berdasarkan Hari Ini
    @GET("shift/hari-ini")
    suspend fun getShiftHariIni(): Response<ShiftResponse>

    // Menampilkan Sisa Cuti Karyawan
    @GET("cuti/sisa")
    suspend fun getSisaCuti(): Response<SisaCutiResponse>

    // POST Pengajuan Cuti Karyawan
    @Multipart
    @POST("cuti/ajukan")
    suspend fun ajukanCuti(
        @Part("tanggalMulai") tanggalMulai: RequestBody,
        @Part("tanggalSelesai") tanggalSelesai: RequestBody,
        @Part("jenisCuti") jenisCuti: RequestBody,
        @Part dokumen: MultipartBody.Part
    ): Response<AjukanCutiResponse>

    // Menampilkan Pengajuan-Pengajuan Cuti yang Membutuhkan Approval Karyawan Login
    @GET("approval-cuti")
    suspend fun getPengajuanCuti(): Response<ListPengajuanCutiResponse>

    // Menampilkan Detail Pengajuan-Pengajuan Cuti yang Membutuhkan Approval Karyawan Login
    @GET("approval-cuti/{id}")
    suspend fun getDetailPengajuanCuti(
        @Path("id") id: Int
    ): Response<DetailPengajuanCutiResponse>

    // POST Approve or Reject Pengajuan Cuti
    @FormUrlEncoded
    @POST("approval-cuti/{id}/proses")
    suspend fun postApprovalCuti(
        @Path("id") id: Int,
        @Field("status") status: String,
        @Field("catatan") catatan: String?
    ): Response<ApiResponse>










}