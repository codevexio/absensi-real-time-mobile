package com.example.absen.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {

    private const val BASE_URL = "https://absensi-real-time-production.up.railway.app/api/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Default client tanpa token
    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit tanpa token (buat login atau register biasanya)
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Public access Retrofit tanpa token
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Function buat API yang perlu token
    fun getApiServiceWithToken(token: String): ApiService {
        val clientWithToken = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        val retrofitWithToken = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientWithToken)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofitWithToken.create(ApiService::class.java)
    }
}
