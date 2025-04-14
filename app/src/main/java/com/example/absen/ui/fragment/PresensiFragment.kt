package com.example.absen.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.api.ApiService
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PresensiFragment : Fragment() {

    private lateinit var btnPresensi: Button
    private lateinit var imagePreview: ImageView
    private var imageFile: File? = null
    private var currentLat = 0.0
    private var currentLng = 0.0
    private val api: ApiService by lazy { ApiClient.apiService }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageFile != null) {
            imagePreview.setImageURI(imageFile!!.toUri())
            sendPresensiToAPI(imageFile!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_presensi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnPresensi = view.findViewById(R.id.btnPresensi)
        imagePreview = view.findViewById(R.id.imagePreview)

        btnPresensi.setOnClickListener {
            ambilLokasi()
        }
    }

    private fun ambilLokasi() {
        val locationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        locationProvider.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                bukaKamera()
            } else {
                Toast.makeText(requireContext(), "Gagal ambil lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bukaKamera() {
        val file = File(requireContext().cacheDir, "presensi_${System.currentTimeMillis()}.jpg")
        file.createNewFile()
        imageFile = file

        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        cameraLauncher.launch(uri)
    }

    private fun sendPresensiToAPI(file: File) {
        val token = "Bearer ${getTokenFromPrefs()}"
        val jadwalKerjaId = RequestBody.create("text/plain".toMediaTypeOrNull(), "1") // nanti dinamis
        val lat = RequestBody.create("text/plain".toMediaTypeOrNull(), currentLat.toString())
        val lng = RequestBody.create("text/plain".toMediaTypeOrNull(), currentLng.toString())
        val requestImage = file.asRequestBody("image/*".toMediaTypeOrNull())
        val foto = MultipartBody.Part.createFormData("foto", file.name, requestImage)

        lifecycleScope.launch {
            try {
                val response = api.presensiMasuk(token, jadwalKerjaId, lat, lng, foto)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Presensi berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Presensi gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTokenFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("token", "") ?: ""
    }
}