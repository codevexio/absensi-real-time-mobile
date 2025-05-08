package com.example.absen.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.absen.databinding.FragmentCutiBinding
import com.example.absen.databinding.FragmentPresensiMasukBinding
import java.io.File

class CutiFragment : Fragment(){
    private var _binding: FragmentCutiBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUri: Uri
    private lateinit var imageFile: File
    private val LOCATION_PERMISSION_CODE = 101
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCutiBinding.inflate(inflater, container, false)
        return binding.root
    }
}