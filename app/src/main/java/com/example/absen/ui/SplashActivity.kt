package com.example.absen.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.absen.R
import com.example.absen.api.ApiClient
import com.example.absen.model.LoginRequest
import com.example.absen.model.LoginResponse
import com.example.absen.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi username dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.apiService.login(LoginRequest(username, password))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val login = response.body()!!
                            sessionManager.saveToken(login.token)
                            sessionManager.saveUser(
                                login.karyawan.id,
                                login.karyawan.nama,
                                login.karyawan.golongan,
                                login.karyawan.divisi
                            )
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@SplashActivity, "Login gagal", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@SplashActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}