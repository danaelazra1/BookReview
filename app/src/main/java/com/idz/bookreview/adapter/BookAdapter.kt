package com.idz.bookreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.idz.bookreview.R
import com.idz.bookreview.model.Book

class BookAdapter(
    private var books: List<Book>,
    private val listener: OnBookClickListener
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    interface OnBookClickListener {
        fun onBookSelected(book: Book)
    }

    inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.bookTitle)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val coverImageView: ImageView = view.findViewById(R.id.bookCover)
        private val selectButton: Button = view.findViewById(R.id.selectButton)

        init {
            selectButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBookSelected(books[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.authorName ?: "Author Unknown"
        if (!book.coverUrl.isNullOrEmpty()) {
            holder.coverImageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(book.coverUrl)
                .into(holder.coverImageView)
        } else {
            holder.coverImageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
