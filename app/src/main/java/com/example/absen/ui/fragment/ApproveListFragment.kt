package com.example.absen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.absen.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.absen.adapter.PengajuanAdapter
import com.example.absen.adapter.RiwayatAdapter
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentRiwayatBinding
import com.example.absen.databinding.FragmentVerifikasicutiBinding
import com.example.absen.model.ListPengajuanCuti
import com.example.absen.model.RekapPresensi
import com.example.absen.ui.PresensiFragment
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApproveListFragment : Fragment(), PengajuanAdapter.OnItemClickListener {

    private var _binding: FragmentVerifikasicutiBinding? = null
    private val binding get() = _binding!!
    private lateinit var buttonBack: ImageView
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
        buttonBack = view.findViewById(R.id.back_approval)
        adapter = PengajuanAdapter(requireContext(), listRekap, this)

        binding.hasilPengajuan.layoutManager = LinearLayoutManager(requireContext())
        binding.hasilPengajuan.adapter = adapter

        buttonBack.setOnClickListener {
            val fragment = PresensiFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        fetchRekapPengajuan()
    }

    override fun onDetailClick(item: ListPengajuanCuti) {
        val bundle = Bundle().apply {
            putInt("id", item.id) // pastikan ListPengajuanCuti punya id
        }

        val detailFragment = DetailPengajuanFragment() // pastikan ada fragment ini
        detailFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack(null)
            .commit()
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
                    .getPengajuanCuti()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val pengajuanResponse = response.body()!!
                        val listData = pengajuanResponse.data
                        if (listData.isNotEmpty()) {
                            listRekap.clear()
                            listRekap.addAll(listData)
                            adapter.notifyDataSetChanged()
                            binding.hasilPengajuan.visibility = View.VISIBLE
                        } else {
                            binding.hasilPengajuan.visibility = View.GONE
                            Toast.makeText(requireContext(), "Data kosong", Toast.LENGTH_SHORT).show()
                        }
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