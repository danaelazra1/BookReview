package com.idz.bookreview.model

data class BookResponse(
    val docs: List<BookData>?
)

data class BookData(
    val title: String?,
    val author_name: List<String>?,
    val cover_i: Int?
) {
    fun toBook(): Book {
        return Book(
            title = title ?: "Unknown",
            authorName = author_name?.firstOrNull() ?: "Unknown",
            coverUrl = cover_i?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
        )
    }
}
