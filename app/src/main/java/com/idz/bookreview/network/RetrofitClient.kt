package com.idz.bookreview.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.idz.bookreview.api.BookApiService

object RetrofitClient {
    private const val BASE_URL = "https://openlibrary.org/"

    val instance: BookApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApiService::class.java)
    }
}
