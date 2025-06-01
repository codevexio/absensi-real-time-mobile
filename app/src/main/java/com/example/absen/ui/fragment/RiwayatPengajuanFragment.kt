package com.example.absen.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.adapter.PengajuanAdapter
import com.example.absen.adapter.RiwayatAdapter
import com.example.absen.adapter.RiwayatPengajuanAdapter
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentRiwayatBinding
import com.example.absen.databinding.FragmentRiwayatPengajuanBinding
import com.example.absen.databinding.FragmentVerifikasicutiBinding
import com.example.absen.model.ListPengajuanCuti
import com.example.absen.model.RekapPresensi
import com.example.absen.model.RiwayatPengajuanCuti
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RiwayatPengajuanFragment : Fragment(), RiwayatPengajuanAdapter.OnItemClickListener{
    private var _binding: FragmentRiwayatPengajuanBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RiwayatPengajuanAdapter
    private val riwayat = mutableListOf<RiwayatPengajuanCuti>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRiwayatPengajuanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        adapter = RiwayatPengajuanAdapter(requireContext(), riwayat, this)
        binding.hasilRiwayatPengajuan.layoutManager = LinearLayoutManager(requireContext())
        binding.hasilRiwayatPengajuan.adapter = adapter

        fetchRiwayatPengajuan()
    }

    override fun onDetailClick(item: RiwayatPengajuanCuti) {
        val bundle = Bundle().apply {
            putInt("id", item.id)
        }

        val detailFragment = DetailRiwayatPengajuanCutiFragment()
        detailFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun fetchRiwayatPengajuan() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getApiServiceWithToken(token).getRiwayatPengajuan()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (!data.isNullOrEmpty()) {
                            riwayat.clear()
                            riwayat.addAll(data)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(requireContext(), "Data riwayat kosong", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("RiwayatPengajuan", "Error: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RiwayatPengajuan", "Exception: ${e.message}")
                }
            }
        }
    }

}