package com.idz.bookreview.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

object CloudinaryService {

    private const val CLOUD_NAME = "df8odu4s4"
    const val UPLOAD_PRESET = "profile_pictures_upload"

    private val retrofit by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/$CLOUD_NAME/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val api: CloudinaryApi by lazy {
        retrofit.create(CloudinaryApi::class.java)
    }
}

interface CloudinaryApi {
    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Query("upload_preset") uploadPreset: String = CloudinaryService.UPLOAD_PRESET
    ): CloudinaryResponse
}

data class CloudinaryResponse(
    @SerializedName("secure_url") val secureUrl: String
)