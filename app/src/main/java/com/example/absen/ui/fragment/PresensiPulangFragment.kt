package com.example.absen.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.model.PresensiMasukResponse
import com.example.absen.ui.PresensiFragment
import com.example.absen.util.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PresensiPulangFragment : Fragment(), OnMapReadyCallback {

    private lateinit var lokasiPulang: TextView
    private lateinit var waktuPulang: TextView
    private lateinit var jadwalKerja: TextView
    private lateinit var fotoImageView: ImageView
    private lateinit var buttonPulang: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager
    private lateinit var googleMap: GoogleMap
    private lateinit var back: ImageView

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var imageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menghubungkan layout XML dengan fragment ini
        return inflater.inflate(R.layout.fragment_presensi_pulang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Inisialisasi komponen
        lokasiPulang = view.findViewById(R.id.lokasiPulang)
        waktuPulang = view.findViewById(R.id.waktuPulang)
        jadwalKerja = view.findViewById(R.id.shift)
        fotoImageView = view.findViewById(R.id.foto)
        buttonPulang = view.findViewById(R.id.buttonPulang)
        back = view.findViewById(R.id.back)

        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Ambil lokasi saat ini
        getCurrentLocation()

        // Ambil shift saat ini
        getShiftHariIni()

        // Tampilkan waktu saat ini
        val currentTime = Calendar.getInstance().time.toString()
        waktuPulang.text = currentTime

        // Saat gambar ditekan, buka kamera
        fotoImageView.setOnClickListener {
            openCamera()
        }

        // Saat tombol "Kirim" ditekan, kirim data presensi ke server
        buttonPulang.setOnClickListener {
            if (imageFile == null) {
                Toast.makeText(requireContext(), "Harap ambil foto terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadPresensi()
        }

        back.setOnClickListener {
            val presensiFragment = PresensiFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.container, presensiFragment)
            transaction.commit()
        }

        // Inisialisasi SupportMapFragment dan panggil onMapReady
        val mapFragment = childFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Ambil lokasi saat ini dari GPS
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        // Ambil lokasi terakhir
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude

                    // Ubah latitude & longitude jadi nama jalan
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        lokasiPulang.text = address
                    } else {
                        lokasiPulang.text = "Tidak dapat mendeteksi lokasi"
                    }

                    // Menambahkan marker di peta
                    addMarkerToMap()
                }
            }
    }

    // Menambahkan marker ke peta berdasarkan lokasi
    private fun addMarkerToMap() {
        val lokasi = LatLng(latitude, longitude)
        googleMap.clear() // Clear previous markers if any
        googleMap.addMarker(
            MarkerOptions().position(lokasi).title("Lokasi Anda")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 17f))
    }

    // Ambil shift
    private fun getShiftHariIni() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiClient.getApiServiceWithToken(token)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getShiftHariIni("Bearer $token") // Kirim token dengan format Bearer
                }

                if (response.isSuccessful && response.body() != null) {
                    val shift = response.body()?.shift // Mendapatkan data shift dari respons API
                    if (shift != null) {
                        // Tampilkan nama shift pada TextView jadwalKerja
                        jadwalKerja.text = shift.nama ?: "Shift tidak ditemukan"
                    } else {
                        Toast.makeText(requireContext(), "Data shift tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data shift", Toast.LENGTH_SHORT).show()
                    Log.e("PresensiPulang", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PresensiPulang", e.stackTraceToString())
            }
        }
    }



    // Buka kamera untuk ambil foto
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startForResult.launch(cameraIntent)
    }

    // Callback saat kamera selesai mengambil gambar
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photo = result.data?.extras?.get("data") as Bitmap
                fotoImageView.setImageBitmap(photo)

                // Simpan gambar ke file sementara
                imageFile = bitmapToFile(photo, "presensi_pulang.jpg")
            }
        }

    // Convert Bitmap menjadi File agar bisa diupload
    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(requireContext().cacheDir, fileName)
        file.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

        return file
    }

    // Kirim data presensi pulang ke API Laravel
    private fun uploadPresensi() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRequest = imageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imagePulang", imageFile!!.name, imageRequest)

        val latitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude.toString())
        val longitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude.toString())

        val api = ApiClient.getApiServiceWithToken(token)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.presensiPulang(imagePart, latitudePart, longitudePart)
                }

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(requireContext(), "Presensi berhasil dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengirim presensi", Toast.LENGTH_SHORT).show()
                    Log.e("PresensiError", response.errorBody()?.string() ?: "Unknown error")
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PresensiException", e.stackTraceToString())
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getCurrentLocation() // Memanggil getCurrentLocation setelah peta siap
    }
}
