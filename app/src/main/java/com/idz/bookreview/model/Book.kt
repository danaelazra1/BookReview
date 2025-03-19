package com.idz.bookreview.model

data class Book(
    val title: String?,
    val authorName: List<String>?,
    val firstPublishYear: String?,
    val coverUrl: String?
)
