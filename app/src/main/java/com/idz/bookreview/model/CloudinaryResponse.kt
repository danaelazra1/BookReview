package com.idz.bookreview.model

import com.google.gson.annotations.SerializedName

data class CloudinaryResponse(
    @SerializedName("resources") val resources: List<CloudinaryImage>
)

data class CloudinaryImage(
    @SerializedName("secure_url") val secureUrl: String
)
