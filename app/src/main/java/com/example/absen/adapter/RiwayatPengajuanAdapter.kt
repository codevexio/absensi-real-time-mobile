package com.example.absen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.model.RiwayatPengajuanCuti

class RiwayatPengajuanAdapter (
    private val context: Context,
    private val riwayat: List<RiwayatPengajuanCuti>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RiwayatPengajuanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggalPengajuan: TextView = itemView.findViewById(R.id.ou_tanggal_pengajuan)
        val tvJenisCuti: TextView = itemView.findViewById(R.id.ou_jenis_cuti)
        val btnDetail: Button = itemView.findViewById(R.id.btn_detail)
    }

    interface OnItemClickListener{
        fun onDetailClick(item: RiwayatPengajuanCuti)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detil_riwayat_pengajuan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = riwayat.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = riwayat[position]
        holder.tvTanggalPengajuan.text = "Tanggal Pengajuan : ${item.tanggal_pengajuan}"
        holder.tvJenisCuti.text = "Jenis Cuti : ${item.jenis_cuti}"

        holder.btnDetail.setOnClickListener {
            listener.onDetailClick(item)
        }
    }
}