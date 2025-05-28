package com.example.absen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.absen.model.KeterlambatanData
import com.example.absen.R

class KeterlambatanAdapter(
    private val context: Context,
    private val listTerlambat: List<KeterlambatanData>
) : RecyclerView.Adapter<KeterlambatanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val gambarKaryawan: ImageView = itemView.findViewById(R.id.gambar_karyawan)
        val namaKaryawan: TextView = itemView.findViewById(R.id.ou_nama)
        val divisiKaryawan: TextView = itemView.findViewById(R.id.ou_divisi)
        val waktuMasuk: TextView = itemView.findViewById(R.id.ou_waktumasuk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.detail_keterlambatan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listTerlambat.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listTerlambat[position]
        holder.namaKaryawan.text = "Nama : ${item.nama_karyawan}"
        holder.divisiKaryawan.text = "Divisi : ${item.divisi_karyawan}"
        holder.waktuMasuk.text = "Waktu Masuk : ${item.waktuMasuk}"

        // Load Gambar
        Glide.with(holder.itemView.context)
            .load(item.imageMasuk)
            .placeholder(R.drawable.empty_photo)
            .into(holder.gambarKaryawan)
    }
}
