package com.example.absen.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.absen.api.ApiClient
import com.example.absen.api.ApiService
import com.example.absen.databinding.FragmentPresensiMasukBinding
import com.example.absen.model.LokasiMasuk
import com.example.absen.model.PresensiMasukRequest
import com.example.absen.model.PresensiMasukResponse
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PresensiMasukFragment : Fragment() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnAmbilGambar: Button
    private lateinit var btnKirimPresensi: Button
    private lateinit var txtLatitude: TextView
    private lateinit var txtLongitude: TextView

    private var imageUri: Uri? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String = "" // Untuk menyimpan path gambar

    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token: String

    // Cek izin lokasi
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    // Menggunakan ActivityResultContracts untuk menangani hasil foto
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                Log.d("PresensiMasukFragment", "Gambar berhasil diambil!")
                imgPreview.setImageURI(imageUri)
            } else {
                Log.e("PresensiMasukFragment", "Gagal mengambil gambar")
                Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPresensiMasukBinding.inflate(inflater, container, false)

        imgPreview = binding.imgPreview
        btnAmbilGambar = binding.btnAmbilGambar
        btnKirimPresensi = binding.btnKirimPresensi
        txtLatitude = binding.txtLatitude
        txtLongitude = binding.txtLongitude

        sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        apiService = ApiClient.getApiServiceWithToken(token)

        // Button Ambil Gambar
        btnAmbilGambar.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // Button Kirim Presensi
        btnKirimPresensi.setOnClickListener {
            kirimPresensiMasuk()
        }

        // Mendapatkan lokasi otomatis
        checkLocationPermission()

        return binding.root
    }

    // Fungsi untuk memeriksa izin lokasi
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Jika izin sudah diberikan, kita bisa mengakses lokasi
            getLokasi()
        } else {
            // Jika izin belum diberikan, kita minta izin
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = createImageFile()
                photoFile?.also {
                    imageUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.absen.fileprovider",
                        it
                    )
                    Log.d("PresensiMasukFragment", "Photo URI: $imageUri") // Log URI foto
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    takePictureLauncher.launch(imageUri)
                }
            }
        }
    }


    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        currentPhotoPath = imageFile.absolutePath // Simpan path gambar
        return imageFile
    }

    @SuppressLint("MissingPermission")
    private fun getLokasi() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    txtLatitude.text = "Latitude: $latitude"
                    txtLongitude.text = "Longitude: $longitude"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kirimPresensiMasuk() {
        val file = File(currentPhotoPath) // Menggunakan currentPhotoPath
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imageBody = MultipartBody.Part.createFormData("imageMasuk", file.name, requestFile)

        val lokasiMasuk = LokasiMasuk(latitude, longitude)

        // API membutuhkan dua parameter terpisah, yaitu imageBody dan lokasiMasuk
        val presensiRequest = PresensiMasukRequest(imageBody, lokasiMasuk)

        // Panggil API menggunakan coroutine
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.presensiMasuk(imageBody, lokasiMasuk)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Presensi berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Presensi gagal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal mengirim data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imgPreview.setImageURI(imageUri)
        }
    }

    // Menangani hasil permintaan izin lokasi
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Jika izin diberikan, kita dapat mengakses lokasi
                getLokasi()
            } else {
                Toast.makeText(requireContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
