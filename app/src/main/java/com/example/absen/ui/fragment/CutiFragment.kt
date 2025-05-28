package com.example.absen.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.absen.R
import com.example.absen.databinding.FragmentCutiBinding
import com.example.absen.api.ApiClient
import com.example.absen.ui.PresensiFragment
import com.example.absen.util.SessionManager
import com.example.absen.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CutiFragment : Fragment() {

    private lateinit var binding: FragmentCutiBinding
    private lateinit var sessionManager: SessionManager
    private var fileUri: Uri? = null
    private var jenisCutiTerpilih: String = ""

    private val documentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fileUri = it
                binding.foto.setImageURI(it)
                binding.foto.alpha = 1f
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCutiBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        setupSpinner()
        fetchSisaCuti()

        binding.back.setOnClickListener {
            val fragment = PresensiFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }

        binding.frameLayout.setOnClickListener {
            documentLauncher.launch("application/pdf")
        }

        binding.buttonCuti.setOnClickListener {
            submitCuti()
        }

        binding.tanggalMulai.setOnClickListener {
            showDatePickerDialog(binding.tanggalMulai)
        }

        binding.tanggalselesai.setOnClickListener {
            showDatePickerDialog(binding.tanggalselesai)
        }


        return binding.root
    }

    private fun showDatePickerDialog(targetEditText: EditText) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                targetEditText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun setupSpinner() {
        val listJenis = listOf("Cuti Tahunan", "Cuti Panjang")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listJenis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.pilihJenisCuti.adapter = adapter

        binding.pilihJenisCuti.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                jenisCutiTerpilih = listJenis[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                jenisCutiTerpilih = ""
            }
        }
    }

    private fun fetchSisaCuti() {
        val token = sessionManager.getToken() ?: return
        val api = ApiClient.getApiServiceWithToken(token)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getSisaCuti()
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()
                    if (body != null) {
                        binding.sisaTahunan.text = "${body.sisaCutiTahun ?: 0} Hari"
                        binding.sisaPanjang.text = "${body.sisaCutiPanjang ?: 0} Hari"
                    } else {
                        Toast.makeText(requireContext(), "Data kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data cuti", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kesalahan jaringan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitCuti() {
        val token = sessionManager.getToken() ?: return
        val tanggalMulai = binding.tanggalMulai.text.toString()
        val tanggalSelesai = binding.tanggalselesai.text.toString()

        if (tanggalMulai.isEmpty() || tanggalSelesai.isEmpty() || fileUri == null || jenisCutiTerpilih.isEmpty()) {
            Toast.makeText(requireContext(), "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            try {
                val file = FileUtils.getFileFromUri(requireContext(), fileUri!!)
                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file_surat_cuti",
                    file.name,
                    requestFile
                )

                val mulai = tanggalMulai.toRequestBody("text/plain".toMediaTypeOrNull())
                val selesai = tanggalSelesai.toRequestBody("text/plain".toMediaTypeOrNull())
                val jenis = jenisCutiTerpilih.toRequestBody("text/plain".toMediaTypeOrNull())

                val api = ApiClient.getApiServiceWithToken(token)

                val response = withContext(Dispatchers.IO) {
                    api.ajukanCuti(mulai, selesai, jenis, filePart)
                }

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Cuti berhasil diajukan", Toast.LENGTH_SHORT).show()
                    fetchSisaCuti()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "Gagal: $errorBody", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
