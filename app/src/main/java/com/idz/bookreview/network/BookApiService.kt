package com.idz.bookreview.network

import com.idz.bookreview.model.BookResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApiService {
    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String = "AIzaSyAPAw2GmnTI1vbql79Vgq9VApU9MsjMVJc"
    ): Call<BookResponse>

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/books/v1/"

        fun create(): BookApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BookApiService::class.java)
        }
    }
}