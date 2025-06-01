package com.example.absen.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentDetailRiwayatPengajuanBinding
import com.example.absen.util.SessionManager
import kotlinx.coroutines.launch

class DetailRiwayatPengajuanCutiFragment : Fragment() {
    private var _binding: FragmentDetailRiwayatPengajuanBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private var cutiId: Int = -1
    private lateinit var tableLayout: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailRiwayatPengajuanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        cutiId = arguments?.getInt("id") ?: -1

        tableLayout = binding.tablePresensi

        if (cutiId != -1) {
            getDetailCuti(cutiId)
        }
    }

    private fun getDetailCuti(id: Int) {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiServiceWithToken(token).getDetailRiwayat(id)
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        // Set data utama
                        binding.namaKaryawan.text = "Nama Pengaju : ${it.nama_pengaju}"
                        binding.jenisCuti.text = "Jenis Cuti : ${it.jenis_cuti}"
                        binding.jumlahCuti.text = "Jumlah Cuti : ${it.jumlah_cuti_diambil}"
                        binding.alasanPenolakan.text = "Alasan Penolakan : ${it.alasan_penolakan ?: "-"}"

                        // Tambahkan data ke TableLayout
                        for (approval in it.status_approval) {
                            val tableRow = TableRow(requireContext())

                            val roleView = TextView(requireContext()).apply {
                                text = approval.role
                                textSize = 10f
                                gravity = Gravity.CENTER
                                setPadding(10, 10, 10, 10)
                            }

                            val namaView = TextView(requireContext()).apply {
                                text = approval.nama
                                textSize = 10f
                                gravity = Gravity.CENTER
                                setPadding(10, 10, 10, 10)
                            }

                            val statusView = TextView(requireContext()).apply {
                                text = approval.status
                                textSize = 10f
                                gravity = Gravity.CENTER
                                setPadding(10, 10, 10, 10)
                            }

                            tableRow.addView(roleView)
                            tableRow.addView(namaView)
                            tableRow.addView(statusView)

                            tableLayout.addView(tableRow)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal ambil detail", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}