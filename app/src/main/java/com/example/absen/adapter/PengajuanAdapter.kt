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
    private val listRekap: List<ListPengajuanCuti>
) : RecyclerView.Adapter<PengajuanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama : TextView = itemView.findViewById(R.id.namapengajuan)
        val tanggal : TextView = itemView.findViewById(R.id.tanggalpengajuan)
        val btnSelengkap: Button = itemView.findViewById(R.id.btn_selengkap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detil_karyawancuti, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRekap[position]
        holder.nama.text = "Nama : ${item.karyawan.nama}"
        holder.tanggal.text = "Tanggal Pengajuan : ${item.created_at}"

        holder.btnSelengkap.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return listRekap.size
    }
}
