package com.idz.bookreview.model

import com.google.gson.annotations.SerializedName

data class BookResponse(
    @SerializedName("items") val items: List<BookItem>?
)

data class BookItem(
    @SerializedName("volumeInfo") val volumeInfo: BookInfo
)

data class BookInfo(
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: List<String>? = null,
    @SerializedName("publishedDate") val publishedDate: String? = null,
    @SerializedName("imageLinks") val imageLinks: ImageLinks? = null
)

data class ImageLinks(
    @SerializedName("thumbnail") val thumbnail: String?
)

data class Book(
    val title: String,
    val authors: List<String>? = null,
    val publishedDate: String? = null,
    val imageLinks: String? = null
)
