package com.example.absen.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
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
import com.example.absen.ui.PresensiFragment
import com.example.absen.util.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PresensiMasukFragment : Fragment(), OnMapReadyCallback {

    private lateinit var lokasiMasuk: TextView
    private lateinit var waktuMasuk: TextView
    private lateinit var jadwalKerja: TextView
    private lateinit var fotoImageView: ImageView
    private lateinit var buttonMasuk: Button
    private lateinit var refresh: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager
    private var googleMap: GoogleMap? = null
    private lateinit var back: ImageView

    private var latitude = 0.0
    private var longitude = 0.0
    private var imageFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_presensi_masuk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lokasiMasuk = view.findViewById(R.id.lokasiMasuk)
        waktuMasuk = view.findViewById(R.id.waktuMasuk)
        jadwalKerja = view.findViewById(R.id.shift)
        fotoImageView = view.findViewById(R.id.foto)
        buttonMasuk = view.findViewById(R.id.buttonMasuk)
        back = view.findViewById(R.id.back)
        refresh = view.findViewById(R.id.refresh_masuk)

        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        getShiftHariIni()

        waktuMasuk.text = Calendar.getInstance().time.toString()

        fotoImageView.setOnClickListener {
            checkCameraPermission()
        }

        buttonMasuk.setOnClickListener {
            if (imageFile == null) {
                Toast.makeText(requireContext(), "Harap ambil foto terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if (latitude == 0.0 || longitude == 0.0) {
                Toast.makeText(requireContext(), "Lokasi belum tersedia", Toast.LENGTH_SHORT).show()
            } else {
                uploadPresensi()
            }
        }

        back.setOnClickListener {
            val fragment = PresensiFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        refresh.setOnClickListener {
            val fragment = PresensiMasukFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            getCurrentLocation()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1002)
        } else {
            openCamera()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jika belum diberi izin, minta izin atau tampilkan pesan
            lokasiMasuk.text = "Izin lokasi belum diberikan"
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addressList.isNullOrEmpty()) {
                    lokasiMasuk.text = addressList[0].getAddressLine(0)
                } else {
                    lokasiMasuk.text = "Tidak dapat mendeteksi alamat"
                }

                val lokasi = LatLng(latitude, longitude)
                googleMap?.apply {
                    clear()
                    addMarker(MarkerOptions().position(lokasi).title("Lokasi Anda"))
                    moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 17f))
                }
            } ?: run {
                lokasiMasuk.text = "Lokasi tidak tersedia"
            }
        }.addOnFailureListener {
            lokasiMasuk.text = "Gagal mendapatkan lokasi"
        }
    }

    private fun getShiftHariIni() {
        val token = sessionManager.getToken() ?: return
        val api = ApiClient.getApiServiceWithToken(token)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getShiftHariIni()
                }
                if (response.isSuccessful && response.body() != null) {
                    jadwalKerja.text = response.body()?.shift?.nama ?: "Shift tidak ditemukan"
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data shift", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startForResult.launch(cameraIntent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                fotoImageView.setImageBitmap(it)
                imageFile = bitmapToFile(it, "presensi_masuk.jpg")
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(requireContext().cacheDir, fileName)
        file.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()
        FileOutputStream(file).apply {
            write(bitmapData)
            flush()
            close()
        }
        return file
    }

    private fun uploadPresensi() {
        val token = sessionManager.getToken() ?: return
        val api = ApiClient.getApiServiceWithToken(token)

        val imageRequest = imageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imageMasuk", imageFile!!.name, imageRequest)
        val latitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude.toString())
        val longitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude.toString())

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.presensiMasuk(imagePart, latitudePart, longitudePart)
                }
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Presensi berhasil", Toast.LENGTH_SHORT).show()

                    // Redirect ke PresensiFragment
                    val fragment = PresensiFragment()
                    val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )

                    transaction.replace(R.id.container, fragment).commit()
                } else {
                    Toast.makeText(requireContext(), "Gagal presensi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) getCurrentLocation()
            1002 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera()
        }
    }
}
