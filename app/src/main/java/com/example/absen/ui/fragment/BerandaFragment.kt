package com.example.absen.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.adapter.KeterlambatanAdapter
import com.example.absen.api.ApiClient
import com.example.absen.api.ApiClient.apiService
import com.example.absen.model.KeterlambatanData
import com.example.absen.model.StatistikKehadiranResponse
import com.example.absen.ui.fragment.KeterlambatanFragment
import com.example.absen.ui.fragment.RiwayatPengajuanFragment
import com.example.absen.util.SessionManager
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException
import kotlinx.coroutines.launch

class BerandaFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: KeterlambatanAdapter
    private val listTerlambat = mutableListOf<KeterlambatanData>()
    private lateinit var statusPresensiTextView: TextView
    private lateinit var jumlahKehadiranTextView: TextView
    private lateinit var jumlahKeterlambatanTextView: TextView
    private lateinit var lihatSelengkapnya : Button
    private lateinit var btnPresensi: Button
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnLihatRiwayat: Button
    private lateinit var cardRiwayatPengajuan: View
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        statusPresensiTextView = view.findViewById(R.id.statuspresensi)
        jumlahKehadiranTextView = view.findViewById(R.id.jumlahkehadiran)
        jumlahKeterlambatanTextView = view.findViewById(R.id.jumlahketerlambatan)
        lihatSelengkapnya = view.findViewById(R.id.lihatselanjutnya)
        btnPresensi = view.findViewById(R.id.presensi)
        btnLihatRiwayat = view.findViewById(R.id.button_lihat_riwayat)
        cardRiwayatPengajuan = view.findViewById(R.id.card1)
        recyclerView = view.findViewById(R.id.listTerlambat)
        btnLogout = view.findViewById(R.id.logout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = KeterlambatanAdapter(requireContext(), listTerlambat)
        recyclerView.adapter = adapter

        tvWelcome = view.findViewById(R.id.tvWelcome)
        val namaUser = sessionManager.getUsername()
        tvWelcome.text = "Selamat Datang $namaUser"

        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val calendar = Calendar.getInstance()
        val locale = Locale("id", "ID") // Setting ke Indonesia
        val dateFormat = SimpleDateFormat("EEEE , dd MMMM yyyy", locale)
        val tanggalHariIni = dateFormat.format(calendar.time)
        tvTanggal.text = tanggalHariIni

        // Event tombol presensi
        btnPresensi.setOnClickListener {
            val fragment = PresensiFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        lihatSelengkapnya.setOnClickListener {
            val fragment = KeterlambatanFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        btnLihatRiwayat.setOnClickListener {
            val fragment = RiwayatPengajuanFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        btnLogout.setOnClickListener {
            sessionManager.clear()
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        getStatistikKehadiran()
        getStatusPresensi()
        getKaryawanTerlambatHariIni()
        cekRiwayatPengajuan()
    }

    private fun getStatistikKehadiran() {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val api = ApiClient.getApiServiceWithToken(token)
        lifecycleScope.launch {
            try {
                val response = api.getStatistikKehadiran()
                if (response.isSuccessful) {
                    val data: StatistikKehadiranResponse? = response.body()
                    jumlahKehadiranTextView.text = data?.jumlahTepatWaktu.toString()
                    jumlahKeterlambatanTextView.text = data?.jumlahTerlambat.toString()
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat statistik", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Cek koneksi internet Anda", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getStatusPresensi() {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val api = ApiClient.getApiServiceWithToken(token)
        lifecycleScope.launch {
            try {
                val response = api.cekStatusPresensi()
                if (response.isSuccessful) {
                    val data = response.body()
                    statusPresensiTextView.text = data?.message ?: "Status tidak diketahui"

                    when (data?.code) {
                        "SUDAH_PRESENSI" -> {
                            btnPresensi.visibility = View.GONE
                            statusPresensiTextView.text = "Status : Sudah presensi masuk"
                        }
                        "CUTI" -> {
                            btnPresensi.visibility = View.GONE
                            statusPresensiTextView.text = "Status : Anda sedang cuti"
                        }
                        "BELUM_PRESENSI" -> {
                            btnPresensi.visibility = View.VISIBLE
                            statusPresensiTextView.text = "Status : Belum presensi masuk"
                        }
                        else -> {
                            btnPresensi.visibility = View.GONE
                            statusPresensiTextView.text = "Terjadi kesalahan"
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat status presensi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Cek koneksi internet Anda", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getKaryawanTerlambatHariIni() {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val api = ApiClient.getApiServiceWithToken(token)
        lifecycleScope.launch {
            try {
                val response = api.getDaftarTerlambat()
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        listTerlambat.clear()
                        listTerlambat.addAll(it)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data keterlambatan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Cek koneksi internet Anda", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cekRiwayatPengajuan() {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            cardRiwayatPengajuan.visibility = View.GONE
            Log.e("RiwayatPengajuan", "Token kosong")
            return
        }

        val api = ApiClient.getApiServiceWithToken(token)
        lifecycleScope.launch {
            try {
                val response = api.getRiwayatPengajuan()
                if (response.isSuccessful) {
                    val data = response.body()
                    if (!data.isNullOrEmpty()) {
                        cardRiwayatPengajuan.visibility = View.VISIBLE
                    } else {
                        cardRiwayatPengajuan.visibility = View.GONE
                    }
                } else {
                    cardRiwayatPengajuan.visibility = View.GONE
                    Log.e("RiwayatPengajuan", "Gagal load: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                cardRiwayatPengajuan.visibility = View.GONE
                Log.e("RiwayatPengajuan", "Network error: ${e.message}")
            } catch (e: HttpException) {
                cardRiwayatPengajuan.visibility = View.GONE
                Log.e("RiwayatPengajuan", "HTTP error: ${e.message}")
            } catch (e: Exception) {
                cardRiwayatPengajuan.visibility = View.GONE
                Log.e("RiwayatPengajuan", "Unexpected error: ${e.message}")
            }
        }
    }


}

