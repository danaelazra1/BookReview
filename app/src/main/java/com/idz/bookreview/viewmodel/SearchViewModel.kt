package com.idz.bookreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.bookreview.model.Book
import com.idz.bookreview.model.BookResponse
import com.idz.bookreview.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel : ViewModel() {

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun searchBooks(query: String) {
        _isLoading.postValue(true)
        _errorMessage.postValue(null)

        RetrofitClient.instance.searchBooks(query).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                _isLoading.postValue(false)

                if (response.isSuccessful) {
                    val bookList = response.body()?.docs?.map { it.toBook() } ?: emptyList()

                    if (bookList.isEmpty()) {
                        _errorMessage.postValue("No results found")
                    }

                    _books.postValue(bookList)
                } else {
                    _errorMessage.postValue("Error when retrieving the data")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("Error: ${t.message}")
            }
        })
    }
}
