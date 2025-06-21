package com.example.absen.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.absen.R
import com.example.absen.databinding.FragmentDetailverifikasiBinding
import com.example.absen.api.ApiClient
import com.example.absen.model.DetailPengajuanCutiResponse
import com.example.absen.ui.PresensiFragment
import com.example.absen.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailPengajuanFragment : Fragment() {

    private var _binding: FragmentDetailverifikasiBinding? = null
    private val binding get() = _binding!!

    private var cutiId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailverifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cutiId = arguments?.getInt("id") ?: 0
        loadDetailCuti()

        binding.buttonApprove.setOnClickListener {
            val radioSetuju = binding.radioSetuju.isChecked
            val radioTolak = binding.radioTolak.isChecked

            if (!radioSetuju && !radioTolak) {
                Toast.makeText(requireContext(), "Pilih persetujuan atau penolakan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (radioSetuju) {
                kirimPersetujuan(true, null)
            } else if (radioTolak) {
                showDialogAlasan()
            }
        }

        binding.back.setOnClickListener {
            val fragment = ApproveListFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment).commit()
        }
    }

    private fun loadDetailCuti() {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.getApiServiceWithToken(token)
                    .getDetailPengajuanCuti(cutiId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val detail: DetailPengajuanCutiResponse = response.body()!!

                        binding.namaKaryawan.text = detail.nama_karyawan
                        binding.tanggalpengajuan.text = detail.tanggal_pengajuan
                        binding.tanggalMulai.text = detail.tanggal_mulai
                        binding.tanggalselesai.text = detail.tanggal_selesai

                        if (!detail.file_surat_cuti.isNullOrEmpty()) {
                            binding.dokumencuti.text = detail.file_surat_cuti
                            Linkify.addLinks(binding.dokumencuti, Linkify.WEB_URLS)

                            binding.dokumencuti.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(detail.file_surat_cuti)
                                }
                                startActivity(intent)
                            }
                        } else {
                            binding.dokumencuti.text = "Dokumen tidak tersedia"
                        }

                    } else {
                        Toast.makeText(requireContext(), "Gagal mengambil detail cuti", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDialogAlasan() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialogalasan, null)
        val editTextAlasan = dialogView.findViewById<EditText>(R.id.editTextAlasan)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Alasan Penolakan")
            .setView(dialogView)
            .setPositiveButton("Kirim", null)  // Dikasih null supaya bisa custom validasi
            .setNegativeButton("Batal") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val alasan = editTextAlasan.text.toString().trim()
                if (alasan.isEmpty()) {
                    editTextAlasan.error = "Alasan wajib diisi"
                    editTextAlasan.requestFocus()
                } else {
                    dialog.dismiss()
                    kirimPersetujuan(false, alasan)
                }
            }
        }

        dialog.show()
    }

    private fun kirimPersetujuan(setuju: Boolean, alasan: String?) {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val status = if (setuju) "Disetujui" else "Ditolak"
                val response = ApiClient.getApiServiceWithToken(token)
                    .postApprovalCuti(cutiId, status, alasan)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Berhasil mengirim persetujuan", Toast.LENGTH_SHORT).show()
                         
                        // Redirect ke PresensiFragment
                        val fragment = PresensiFragment()
                        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                        transaction.setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )

                        transaction.replace(R.id.container, fragment).commit()
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengirim persetujuan", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
