package com.idz.bookreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.model.BookInfo
import com.squareup.picasso.Picasso

class BookAdapter(private var books: List<BookInfo>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.bookTitle)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val bookImageView: ImageView = view.findViewById(R.id.bookImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.authors?.joinToString(", ") ?: "לא ידוע"

        val imageUrl = book.imageLinks?.thumbnail
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_book_placeholder).into(holder.bookImageView)
        } else {
            holder.bookImageView.setImageResource(R.drawable.ic_book_placeholder)
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<BookInfo>) {
        books = newBooks
        notifyDataSetChanged()
    }
}



