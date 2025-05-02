package com.example.absen.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log  // <-- Import Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentPresensiMasukBinding
import com.example.absen.util.SessionManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class PresensiMasukFragment : Fragment() {

    private var _binding: FragmentPresensiMasukBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUri: Uri
    private lateinit var imageFile: File
    private val LOCATION_PERMISSION_CODE = 101
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d("PresensiMasukFragment", "Gambar berhasil diambil")
            binding.imgPreview.setImageURI(imageUri)
        } else {
            Log.e("PresensiMasukFragment", "Gagal mengambil gambar")
            Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPresensiMasukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("PresensiMasukFragment", "onViewCreated")

        setupImageFile()
        checkLocationPermission()

        binding.btnAmbilGambar.setOnClickListener {
            Log.d("PresensiMasukFragment", "Tombol ambil gambar ditekan")
            cameraLauncher.launch(imageUri)
        }

        binding.btnKirimPresensi.setOnClickListener {
            val sessionManager = SessionManager(requireContext())
            val token = sessionManager.getToken()

            Log.d("PresensiMasukFragment", "Token yang didapat: $token")

            if (!imageFile.exists()) {
                Log.e("PresensiMasukFragment", "File gambar tidak ada")
                Toast.makeText(requireContext(), "Silakan ambil gambar dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentLatitude != null && currentLongitude != null && token != null) {
                Log.d("PresensiMasukFragment", "Mempersiapkan untuk mengirim presensi")
                kirimPresensi(imageFile, currentLatitude!!, currentLongitude!!, token)
            } else {
                Log.e("PresensiMasukFragment", "Lokasi atau token tidak tersedia")
                Toast.makeText(requireContext(), "Lokasi atau token belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupImageFile() {
        val cacheDir = requireContext().cacheDir
        imageFile = File(cacheDir, "presensi_masuk.jpg")
        try {
            if (!imageFile.exists()) {
                imageFile.createNewFile()
                Log.d("PresensiMasukFragment", "File gambar baru dibuat di: ${imageFile.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e("PresensiMasukFragment", "Gagal membuat file gambar", e)
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
            Log.d("PresensiMasukFragment", "Izin lokasi sudah diberikan")
            ambilLokasiSekarang()
        } else {
            Log.d("PresensiMasukFragment", "Meminta izin lokasi")
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
                    Log.d("PresensiMasukFragment", "Lokasi berhasil didapat: Lat=$currentLatitude, Long=$currentLongitude")
                    binding.txtLatitude.text = "Latitude: $currentLatitude"
                    binding.txtLongitude.text = "Longitude: $currentLongitude"
                } ?: run {
                    Log.e("PresensiMasukFragment", "Lokasi tidak tersedia")
                    Toast.makeText(requireContext(), "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e("PresensiMasukFragment", "Gagal mendapatkan lokasi", it)
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kirimPresensi(imageFile: File, latitude: Double, longitude: Double, token: String) {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imageMasuk", imageFile.name, requestFile)

        val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            try {
                Log.d("PresensiMasukFragment", "Mengirim presensi ke API dengan token $token")
                val response = ApiClient.getApiServiceWithToken(token).presensiMasuk(
                    imageMasuk = imagePart,
                    latitude = latitudeBody,
                    longitude = longitudeBody
                )

                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()!!.message
                    Log.d("PresensiMasukFragment", "Presensi berhasil: $message")
                    Toast.makeText(requireContext(), "Sukses: $message", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("PresensiMasukFragment", "Gagal mengirim presensi: $error")
                    Toast.makeText(requireContext(), "Gagal: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PresensiMasukFragment", "Error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PresensiMasukFragment", "Izin lokasi diberikan setelah permintaan")
            ambilLokasiSekarang()
        } else {
            Log.e("PresensiMasukFragment", "Izin lokasi ditolak")
            Toast.makeText(requireContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
