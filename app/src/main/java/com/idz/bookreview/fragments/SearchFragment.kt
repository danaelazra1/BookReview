package com.idz.bookreview.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.BookAdapter
import com.idz.bookreview.api.BookApiService
import com.idz.bookreview.model.BookInfo
import com.idz.bookreview.model.BookResponse
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerViewBooks: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        recyclerViewBooks = view.findViewById(R.id.recyclerViewBooks)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerViewBooks.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter(emptyList())
        recyclerViewBooks.adapter = bookAdapter

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchBooks(query)
            } else {
                Toast.makeText(requireContext(), "נא להזין שם ספר לחיפוש", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchBooks(query: String) {
        progressBar.visibility = View.VISIBLE
        val apiService = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApiService::class.java)

        apiService.searchBooks(query, "AIzaSyAPAw2GmnTI1vbql79Vgq9VApU9MsjMVJc")
            .enqueue(object : Callback<BookResponse> {
                override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val books = response.body()?.items?.map { it.volumeInfo } ?: emptyList()

                        books.forEach { book ->
                            Log.d("SearchFragment", "📘 ספר: ${book.title}, 🖼️ תמונה: ${book.imageLinks?.thumbnail}")
                        }

                        bookAdapter.updateBooks(books)
                    } else {
                        Toast.makeText(requireContext(), "שגיאה בעת הבאת התוצאות", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "שגיאה: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

