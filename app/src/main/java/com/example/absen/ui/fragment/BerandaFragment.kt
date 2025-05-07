package com.example.absen.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.model.StatistikKehadiranResponse
import com.example.absen.util.SessionManager
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.launch

class BerandaFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var statusPresensiTextView: TextView
    private lateinit var jumlahKehadiranTextView: TextView
    private lateinit var jumlahKeterlambatanTextView: TextView
    private lateinit var btnPresensi: Button

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
        btnPresensi = view.findViewById(R.id.presensi)

        // Event tombol presensi
        btnPresensi.setOnClickListener {
            Toast.makeText(requireContext(), "Navigasi ke halaman presensi", Toast.LENGTH_SHORT).show()
            // Di sini kamu bisa arahkan ke PresensiMasukFragment atau PresensiPulangFragment
        }

        getStatistikKehadiran()
        getStatusPresensi()
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
        // Jika kamu punya endpoint `cek-status-presensi`, panggil di sini.
        statusPresensiTextView.text = "Status: Belum"
    }
}