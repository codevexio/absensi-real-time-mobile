package com.example.absen.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentPresensiPulangBinding
import com.example.absen.util.SessionManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class PresensiPulangFragment : Fragment() {

    private var _binding: FragmentPresensiPulangBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUri: Uri
    private lateinit var imageFile: File
    private val LOCATION_PERMISSION_CODE = 101
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.imgPreview.setImageURI(imageUri)
        } else {
            Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPresensiPulangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageFile()
        checkLocationPermission()

        binding.btnAmbilGambar.setOnClickListener {
            cameraLauncher.launch(imageUri)
        }

        binding.btnKirimPresensi.setOnClickListener {
            val sessionManager = SessionManager(requireContext())
            val token = sessionManager.getToken()

            if (!imageFile.exists()) {
                Toast.makeText(requireContext(), "Silakan ambil gambar dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentLatitude != null && currentLongitude != null && token != null) {
                kirimPresensi(imageFile, currentLatitude!!, currentLongitude!!, token)
            } else {
                Toast.makeText(requireContext(), "Lokasi atau token belum tersedia", Toast.LENGTH_SHORT).show()
                if (token == null) Toast.makeText(requireContext(), "Token NULL", Toast.LENGTH_SHORT).show()
                if (currentLatitude == null) Toast.makeText(requireContext(), "Latitude NULL", Toast.LENGTH_SHORT).show()
                if (currentLongitude == null) Toast.makeText(requireContext(), "Longitude NULL", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupImageFile() {
        val cacheDir = requireContext().cacheDir
        imageFile = File(cacheDir, "presensi_pulang.jpg")
        try {
            if (!imageFile.exists()) {
                imageFile.createNewFile()
            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Gagal membuat file gambar", Toast.LENGTH_SHORT).show()
        }

        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ambilLokasiSekarang()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun ambilLokasiSekarang() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                    binding.txtLatitude.text = "Latitude: $currentLatitude"
                    binding.txtLongitude.text = "Longitude: $currentLongitude"
                } ?: run {
                    Toast.makeText(requireContext(), "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kirimPresensi(imageFile: File, latitude: Double, longitude: Double, token: String) {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("imagePulang", imageFile.name, requestFile)

        val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiServiceWithToken(token).presensiPulang(
                    imagePulang = body,
                    latitude = latitudeBody,
                    longitude = longitudeBody
                )

                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()!!.message
                    Toast.makeText(requireContext(), "Sukses: $message", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "Gagal: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getTokenFromSharedPreferences(): String? {
        val sessionManager = SessionManager(requireContext())
        return sessionManager.getToken()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ambilLokasiSekarang()
            } else {
                Toast.makeText(requireContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
