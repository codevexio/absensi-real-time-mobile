package com.example.absen.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.model.RekapPresensi
import com.example.absen.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RiwayatAdapter(
    private val context: Context,
    private val listRekap: List<RekapPresensi>
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBulan: TextView = itemView.findViewById(R.id.ou_bulan)
        val btnDownload: Button = itemView.findViewById(R.id.btn_download)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detil_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listRekap.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRekap[position]
        holder.tvBulan.text = formatBulan(item.bulan)

        holder.btnDownload.setOnClickListener {
            val sessionManager = SessionManager(context)
            val token = sessionManager.getToken()
            if (token != null) {
                downloadPdf(token, item.bulan)
            } else {
                Toast.makeText(context, "Token tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            }
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
                            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            fileName
                        )

                        body.byteStream().use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Tidak ada aplikasi untuk membuka PDF.", Toast.LENGTH_SHORT).show()
                            }

                            Toast.makeText(context, "File berhasil diunduh: ${file.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Gagal mengunduh file: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("RiwayatAdapter", "Download error: ${e.message}")
            }
        }
    }

    private fun formatBulan(bulan: String): String {
        val bulanIndo = mapOf(
            "01" to "Januari", "02" to "Februari", "03" to "Maret",
            "04" to "April", "05" to "Mei", "06" to "Juni",
            "07" to "Juli", "08" to "Agustus", "09" to "September",
            "10" to "Oktober", "11" to "November", "12" to "Desember"
        )
        val parts = bulan.split("-")
        return "${bulanIndo[parts[1]]} ${parts[0]}"
    }
}
