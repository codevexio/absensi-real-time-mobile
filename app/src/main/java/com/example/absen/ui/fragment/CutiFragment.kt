package com.example.absen.ui.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.api.ApiService
import com.example.absen.model.SisaCutiResponse
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CutiFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private var fotoFile: File? = null

    private lateinit var sisaTahunan: TextView
    private lateinit var sisaPanjang: TextView
    private lateinit var tanggalMulai: EditText
    private lateinit var tanggalSelesai: EditText
    private lateinit var pilihJenisCuti: Spinner
    private lateinit var fotoImageView: ImageView
    private lateinit var buttonKirim: Button

    private val jenisCutiList = listOf("Cuti Tahunan", "Cuti Panjang")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cuti, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        apiService = ApiClient.getApiServiceWithToken(sessionManager.getToken() ?: "")

        sisaTahunan = view.findViewById(R.id.sisaTahunan)
        sisaPanjang = view.findViewById(R.id.sisaPanjang)
        tanggalMulai = view.findViewById(R.id.tanggalMulai)
        tanggalSelesai = view.findViewById(R.id.tanggalselesai)
        pilihJenisCuti = view.findViewById(R.id.pilihJenisCuti)
        fotoImageView = view.findViewById(R.id.foto)
        buttonKirim = view.findViewById(R.id.buttonCuti)

        setupSpinner()
        getSisaCuti()
        setupDatePicker()
        setupCamera()
        setupKirim()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisCutiList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pilihJenisCuti.adapter = adapter
    }

    private fun getSisaCuti() {
        val handler = CoroutineExceptionHandler { _, exception ->
            Toast.makeText(requireContext(), "Gagal ambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch(handler) {
            try {
                val response = apiService.getSisaCuti()
                sisaTahunan.text = "${response.data.cutiTahunan} Hari"
                sisaPanjang.text = "${response.data.cutiPanjang} Hari"
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        tanggalMulai.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                tanggalMulai.setText("$year-${month + 1}-$dayOfMonth")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tanggalSelesai.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                tanggalSelesai.setText("$year-${month + 1}-$dayOfMonth")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                fotoImageView.setImageBitmap(it)
                fotoFile = bitmapToFile(it, "dokumen_cuti.jpg")
            }
        }
    }

    private fun setupCamera() {
        fotoImageView.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    private fun setupKirim() {
        buttonKirim.setOnClickListener {
            val mulai = tanggalMulai.text.toString()
            val selesai = tanggalSelesai.text.toString()
            val jenis = if (pilihJenisCuti.selectedItemPosition == 0) "tahunan" else "panjang"

            if (mulai.isBlank() || selesai.isBlank() || fotoFile == null) {
                Toast.makeText(requireContext(), "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tanggalMulaiBody = mulai.toRequestBody("text/plain".toMediaTypeOrNull())
            val tanggalSelesaiBody = selesai.toRequestBody("text/plain".toMediaTypeOrNull())
            val jenisCutiBody = jenis.toRequestBody("text/plain".toMediaTypeOrNull())

            val dokumenBody = fotoFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            val dokumenPart = MultipartBody.Part.createFormData("dokumen", fotoFile!!.name, dokumenBody)

            val handler = CoroutineExceptionHandler { _, exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

            lifecycleScope.launch(handler) {
                try {
                    val response: Response<SisaCutiResponse> = apiService.ajukanCuti(
                        tanggalMulaiBody,
                        tanggalSelesaiBody,
                        jenisCutiBody,
                        dokumenPart
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Pengajuan cuti berhasil", Toast.LENGTH_SHORT).show()
                        tanggalMulai.text.clear()
                        tanggalSelesai.text.clear()
                        fotoImageView.setImageResource(R.drawable.camera)
                        fotoFile = null
                        getSisaCuti()
                    } else {
                        Toast.makeText(requireContext(), "Pengajuan gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Kesalahan saat mengirim: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(requireContext().cacheDir, fileName)
        file.createNewFile()
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
        fos.flush()
        fos.close()
        return file
    }
}
