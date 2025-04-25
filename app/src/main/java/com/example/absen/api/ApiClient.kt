package com.example.absen.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object ApiClient {

    private const val BASE_URL = "https://absensi-real-time-production.up.railway.app/api/"

    // Client dengan Gson yang mengizinkan parsing lenient
    val apiService: ApiService by lazy {
        val gson = GsonBuilder()
            .setLenient()  // Mengaktifkan mode lenient
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    // Client dengan token (untuk request yang butuh autentikasi)
    fun getApiServiceWithToken(token: String): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
