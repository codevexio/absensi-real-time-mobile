package com.example.absen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.model.RekapPresensi

class RiwayatAdapter(
    private val context: Context,
    private val listRekap: List<RekapPresensi>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBulan: TextView = itemView.findViewById(R.id.ou_bulan)
        val btnSelengkap: Button = itemView.findViewById(R.id.btn_cek)
    }

    interface OnItemClickListener {
        fun onDetailClick(item: RekapPresensi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detil_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listRekap.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRekap[position]
        holder.tvBulan.text = formatBulan(item.bulan)

        holder.btnSelengkap.setOnClickListener {
            listener.onDetailClick(item)
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
