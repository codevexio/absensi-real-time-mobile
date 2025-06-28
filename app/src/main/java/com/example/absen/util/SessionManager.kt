package com.example.absen.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("absen_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("TOKEN", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun saveUser(id: Int, nama: String, username: String, golongan: String, divisi: String) {
        prefs.edit()
            .putInt("USER_ID", id)
            .putString("USER_NAMA", nama)
            .putString("USERNAME", username)
            .putString("USER_GOLONGAN", golongan)
            .putString("USER_DIVISI", divisi)
            .apply()
    }

    fun fetchKaryawanId(): Int {
        return getUserId()  // Mengambil ID karyawan yang disimpan di SharedPreferences
    }

    fun getUserId(): Int {
        return prefs.getInt("USER_ID", -1)
    }

    fun getUserNama(): String? {
        return prefs.getString("USER_NAMA", null)
    }

    fun getUsername(): String? {
        return prefs.getString("USERNAME", null)
    }

    fun getUserGolongan(): String? {
        return prefs.getString("USER_GOLONGAN", null)
    }

    fun getUserDivisi(): String? {
        return prefs.getString("USER_DIVISI", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
