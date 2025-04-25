package com.example.absen.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.api.ApiService
import com.example.absen.model.CekWaktuPresensiResponse
import com.example.absen.ui.fragment.PresensiMasukFragment
import com.example.absen.ui.fragment.PresensiPulangFragment
import com.example.absen.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PresensiFragment : Fragment(R.layout.fragment_presensi) {

    private lateinit var btnPresensiMasuk: Button
    private lateinit var btnPresensiPulang: Button
    private lateinit var tvMessage: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi UI
        btnPresensiMasuk = view.findViewById(R.id.btn_presensi_masuk)
        btnPresensiPulang = view.findViewById(R.id.btn_presensi_pulang)
        tvMessage = view.findViewById(R.id.tv_message)

        // Inisialisasi SessionManager
        sessionManager = SessionManager(requireContext())

        // Ambil token dari SessionManager
        val token = sessionManager.getToken()

        if (token != null) {
            // Inisialisasi API dengan token
            apiService = ApiClient.getApiServiceWithToken(token)
            cekWaktuPresensi()
        } else {
            tvMessage.text = "Token tidak ditemukan. Silakan login kembali."
            Toast.makeText(requireContext(), "Token tidak ditemukan. Silakan login kembali.", Toast.LENGTH_LONG).show()
        }

        // Navigasi ke PresensiMasukFragment jika tombol presensi masuk ditekan
        btnPresensiMasuk.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, PresensiMasukFragment())  // Ganti dengan PresensiMasukFragment
                .addToBackStack(null)
                .commit()
        }

        // Navigasi ke PresensiPulangFragment jika tombol presensi pulang ditekan
        btnPresensiPulang.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, PresensiPulangFragment())  // Ganti dengan PresensiPulangFragment
                .addToBackStack(null)
                .commit()
        }
    }

    private fun cekWaktuPresensi() {
        apiService.cekWaktuPresensi().enqueue(object : Callback<CekWaktuPresensiResponse> {
            override fun onResponse(
                call: Call<CekWaktuPresensiResponse>,
                response: Response<CekWaktuPresensiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        when {
                            data.bisaPresensiMasuk -> {
                                btnPresensiMasuk.visibility = View.VISIBLE
                                btnPresensiPulang.visibility = View.GONE
                            }
                            data.bisaPresensiPulang -> {
                                btnPresensiMasuk.visibility = View.GONE
                                btnPresensiPulang.visibility = View.VISIBLE
                            }
                            else -> {
                                btnPresensiMasuk.visibility = View.GONE
                                btnPresensiPulang.visibility = View.GONE
                            }
                        }
                        tvMessage.text = data.message
                    } else {
                        tvMessage.text = "Data tidak ditemukan dari server."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    tvMessage.text = "Error ${response.code()}: $errorBody"
                }
            }

            override fun onFailure(call: Call<CekWaktuPresensiResponse>, t: Throwable) {
                tvMessage.text = "Gagal koneksi ke server: ${t.message}"
            }
        })
    }
}
