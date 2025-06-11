package com.example.absen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.adapter.KeterlambatanAdapter
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentKeterlambatanBinding
import com.example.absen.model.KeterlambatanData
import com.example.absen.ui.BerandaFragment
import com.example.absen.ui.PresensiFragment
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class KeterlambatanFragment : Fragment() {

    private var _binding: FragmentKeterlambatanBinding? = null
    private val binding get() = _binding!!
    private lateinit var backButton: ImageView
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: KeterlambatanAdapter
    private val listTerlambat = mutableListOf<KeterlambatanData>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKeterlambatanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        recyclerView = view.findViewById(R.id.listKaryawanTerlambat)
        backButton = view.findViewById(R.id.back_keterlambatan)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = KeterlambatanAdapter(requireContext(), listTerlambat)
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            val fragment = BerandaFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }
        getKaryawanTerlambatHariIni()
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
}