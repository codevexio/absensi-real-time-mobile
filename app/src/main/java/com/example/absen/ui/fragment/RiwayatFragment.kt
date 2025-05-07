package com.example.absen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.absen.adapter.RiwayatAdapter
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentRiwayatBinding
import com.example.absen.model.RekapPresensi
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RiwayatFragment : Fragment() {

    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RiwayatAdapter
    private val listRekap = mutableListOf<RekapPresensi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        adapter = RiwayatAdapter(requireContext(), listRekap)

        binding.hasilRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.hasilRiwayat.adapter = adapter

        fetchRekapPresensi()
    }

    private fun fetchRekapPresensi() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getApiServiceWithToken(token).listRekapPresensi()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        data?.rekap_presensi?.let {
                            listRekap.clear()
                            listRekap.addAll(it)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
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