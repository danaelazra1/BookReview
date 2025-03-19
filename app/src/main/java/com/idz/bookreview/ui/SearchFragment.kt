package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.BookAdapter
import com.idz.bookreview.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerViewBooks: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookAdapter: BookAdapter
    private lateinit var noResultsText: TextView


    private val searchViewModel: SearchViewModel by viewModels()

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
        noResultsText = view.findViewById(R.id.noResultsText)


        recyclerViewBooks.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter(emptyList())
        recyclerViewBooks.adapter = bookAdapter

        searchViewModel.books.observe(viewLifecycleOwner) { books ->
            progressBar.visibility = View.GONE
            if (books.isEmpty()) {
                recyclerViewBooks.visibility = View.GONE
                noResultsText.visibility = View.VISIBLE // ✅ אם אין ספרים, נציג הודעה
            } else {
                recyclerViewBooks.visibility = View.VISIBLE
                noResultsText.visibility = View.GONE
                bookAdapter.updateBooks(books)
            }
        }

        searchViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        searchViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchViewModel.searchBooks(query)
            } else {
                Toast.makeText(requireContext(), "Search for a book", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
