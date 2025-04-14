package com.example.absen.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.absen.R
import com.example.absen.databinding.ActivityMainBinding
import com.example.absen.ui.fragment.BerandaFragment
import com.example.absen.ui.fragment.RiwayatFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeFragment(BerandaFragment())
        binding.navbar.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.beranda -> changeFragment(BerandaFragment())
                R.id.presensi -> changeFragment(PresensiFragment())
                R.id.history -> changeFragment(RiwayatFragment())
                else -> {}
            }
            true
        }
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }
}