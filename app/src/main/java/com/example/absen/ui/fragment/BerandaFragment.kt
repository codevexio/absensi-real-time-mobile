package com.example.absen.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.absen.R
import com.example.absen.util.SessionManager

class BerandaFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Inisialisasi SessionManager
        sessionManager = SessionManager(requireContext())

        // Ambil data user dari SharedPreferences
        val nama = sessionManager.getUserNama() ?: "Nama Tidak Ditemukan"
        val golongan = sessionManager.getUserGolongan() ?: "Golongan Tidak Ditemukan"
        val divisi = sessionManager.getUserDivisi() ?: "Divisi Tidak Ditemukan"

        return view
    }
}
