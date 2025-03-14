package com.idz.bookreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.bookreview.model.Book
import com.idz.bookreview.model.BookInfo
import com.idz.bookreview.model.BookResponse
import com.idz.bookreview.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel : ViewModel() {
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books

    fun searchBooks(query: String) {
        RetrofitClient.instance.searchBooks(query).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val bookList = response.body()?.items?.map {
                        Book(
                            it.volumeInfo.title,
                            it.volumeInfo.authors,
                            it.volumeInfo.publishedDate,
                            it.volumeInfo.imageLinks?.thumbnail
                        )
                    } ?: emptyList()
                    _books.value = bookList
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                _books.value = emptyList()
            }
        })
    }
}

