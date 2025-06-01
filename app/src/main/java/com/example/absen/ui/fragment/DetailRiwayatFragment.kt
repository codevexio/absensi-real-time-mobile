package com.example.absen.ui.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import java.io.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.absen.api.ApiClient
import com.example.absen.databinding.FragmentDetailRiwayatBinding
import com.example.absen.model.DetailPresensi
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailRiwayatFragment : Fragment() {

    private var bulan: String? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var binding: FragmentDetailRiwayatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bulan = arguments?.getString("bulan")
        Log.d("DetailRiwayatFragment", "Received Bulan: $bulan")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        fetchDetailPresensi()

        binding.btnDownloadPdf.setOnClickListener {
            val token = sessionManager.getToken()
            if (token != null && bulan != null) {
                downloadPdf(token, bulan!!)
            } else {
                Toast.makeText(requireContext(), "Token atau bulan tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDetailPresensi() {
        val token = sessionManager.getToken() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getApiServiceWithToken(token).getDetailPresensi(bulan ?: "")
                withContext(Dispatchers.Main) {
                    Log.d("DetailRiwayatFragment", "Status Code: ${response.code()}")
                    if (response.isSuccessful) {
                        // Ganti data() dengan rekap sesuai model data
                        val detailList = response.body()?.rekap ?: emptyList()
                        Log.d("DetailRiwayatFragment", "Data Size: ${detailList.size}")
                        showDataInTable(detailList)
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengambil data detail", Toast.LENGTH_SHORT).show()
                        Log.e("DetailRiwayatFragment", "Error: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DetailRiwayatFragment", "Exception: ${e.stackTraceToString()}")
                }
            }
        }
    }

    private fun showDataInTable(data: List<DetailPresensi>) {
        val table = binding.tablePresensi

        // Hapus semua baris kecuali header (indeks 0)
        val childCount = table.childCount
        if (childCount > 1) {
            table.removeViews(1, childCount - 1)
        }

        for (item in data) {
            val row = TableRow(requireContext())
            row.setPadding(4, 4, 4, 4)
            row.addView(createCell(item.tanggal))
            row.addView(createCell(item.jam_masuk ?: "-"))
            row.addView(createCell(item.status_masuk ?: "-"))
            row.addView(createCell(item.jam_pulang ?: "-"))
            row.addView(createCell(item.status_pulang ?: "-"))
            row.addView(createCell(item.shift ?: "-"))
            table.addView(row)
        }
    }

    private fun createCell(text: String, isHeader: Boolean = false): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            this.setPadding(16, 8, 16, 8)
            this.textSize = if (isHeader) 14f else 12f
            if (isHeader) this.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun downloadPdf(token: String, bulan: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = ApiClient.getApiServiceWithToken(token)
                val response = api.downloadRekapPdf(bulan)

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val fileName = "Rekap_${bulan}.pdf"
                        val file = File(
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            fileName
                        )

                        body.byteStream().use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            val uri: Uri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            if (intent.resolveActivity(requireContext().packageManager) != null) {
                                startActivity(intent)
                            } else {
                                Toast.makeText(requireContext(), "Tidak ada aplikasi untuk membuka PDF.", Toast.LENGTH_SHORT).show()
                            }

                            Toast.makeText(requireContext(), "File berhasil diunduh: ${file.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Gagal mengunduh file: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("DetailRiwayatFragment", "Download error: ${e.message}")
            }
        }
    }


}
