package com.idz.bookreview.api

import com.idz.bookreview.model.BookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApiService {
    @GET("search.json")
    fun searchBooks(
        @Query("q") query: String // 🔹 חיפוש ספרים לפי שם או מחבר
    ): Call<BookResponse>
}
