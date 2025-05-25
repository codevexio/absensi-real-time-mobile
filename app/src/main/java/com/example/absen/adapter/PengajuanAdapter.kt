package com.example.absen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.absen.R
import com.example.absen.model.ListPengajuanCuti

class PengajuanAdapter(
    private val context: Context,
    private val listRekap: List<ListPengajuanCuti>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<PengajuanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama : TextView = itemView.findViewById(R.id.namapengajuan)
        val tanggal : TextView = itemView.findViewById(R.id.tanggalpengajuan)
        val btnSelengkap: Button = itemView.findViewById(R.id.btn_selengkap)
    }

    interface OnItemClickListener {
        fun onDetailClick(item: ListPengajuanCuti)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detil_karyawancuti, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRekap[position]
        holder.nama.text = "Nama : ${item.nama_karyawan}"
        holder.tanggal.text = "Tanggal Pengajuan : ${item.tanggal_pengajuan}"

        holder.btnSelengkap.setOnClickListener {
            listener.onDetailClick(item)
        }
    }

    override fun getItemCount(): Int = listRekap.size
}
