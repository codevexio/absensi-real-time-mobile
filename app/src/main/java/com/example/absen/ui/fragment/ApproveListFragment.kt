package com.example.absen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.absen.adapter.PengajuanAdapter
import com.example.absen.adapter.RiwayatAdapter
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentRiwayatBinding
import com.example.absen.databinding.FragmentVerifikasicutiBinding
import com.example.absen.model.ListPengajuanCuti
import com.example.absen.model.RekapPresensi
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApproveListFragment : Fragment() {

    private var _binding: FragmentVerifikasicutiBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PengajuanAdapter
    private val listRekap = mutableListOf<ListPengajuanCuti>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifikasicutiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        adapter = PengajuanAdapter(requireContext(), listRekap)

        binding.hasilPengajuan.layoutManager = LinearLayoutManager(requireContext())
        binding.hasilPengajuan.adapter = adapter

        fetchRekapPengajuan()
    }

    private fun fetchRekapPengajuan() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getApiServiceWithToken(token)
                    .getPengajuanCutiForApproval("Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        listRekap.clear()
                        listRekap.addAll(response.body()!!)  // <-- ini error sebelumnya karena response bukan List langsung
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Gagal ambil data", Toast.LENGTH_SHORT).show()
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