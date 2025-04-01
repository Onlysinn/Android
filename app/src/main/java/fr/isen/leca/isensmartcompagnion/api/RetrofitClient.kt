package fr.isen.leca.isensmartcompagnion.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


object RetrofitClient {
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/" // Remplace avec l'URL de ton API

    val instance: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convertisseur Gson
            .build()
            .create(EventApiService::class.java)
    }
}