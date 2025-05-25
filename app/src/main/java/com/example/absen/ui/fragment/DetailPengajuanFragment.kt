package com.example.absen.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.databinding.FragmentDetailverifikasiBinding
import com.example.absen.api.ApiClient
import com.example.absen.model.DetailPengajuanCutiResponse
import com.example.absen.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailPengajuanFragment : Fragment() {

    private var _binding: FragmentDetailverifikasiBinding? = null
    private val binding get() = _binding!!

    private var cutiId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailverifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cutiId = arguments?.getInt("id") ?: 0
        loadDetailCuti()
    }

    private fun loadDetailCuti() {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.getApiServiceWithToken(token)
                    .getDetailPengajuanCuti("Bearer $token", cutiId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val detail: DetailPengajuanCutiResponse = response.body()!!

                        binding.namaKaryawan.text = detail.nama_karyawan
                        binding.tanggalpengajuan.text = detail.tanggal_pengajuan
                        binding.tanggalMulai.text = detail.tanggal_mulai
                        binding.tanggalselesai.text = detail.tanggal_selesai

                        if (!detail.file_surat_cuti.isNullOrEmpty()) {
                            binding.dokumencuti.text = detail.file_surat_cuti
                            Linkify.addLinks(binding.dokumencuti, Linkify.WEB_URLS)

                            binding.dokumencuti.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(detail.file_surat_cuti)
                                }
                                startActivity(intent)
                            }
                        } else {
                            binding.dokumencuti.text = "Dokumen tidak tersedia"
                        }

                    } else {
                        Toast.makeText(requireContext(), "Gagal mengambil detail cuti", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
