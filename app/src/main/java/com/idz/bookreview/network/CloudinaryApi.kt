package com.idz.bookreview.network

import com.idz.bookreview.model.CloudinaryResponse
import retrofit2.Call
import retrofit2.http.GET

interface CloudinaryApi {
    @GET("v1_1/df8odu4s4/resources/image")
    fun getImages(): Call<CloudinaryResponse>
}


