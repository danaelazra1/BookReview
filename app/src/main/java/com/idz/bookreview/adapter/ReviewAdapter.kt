package com.idz.bookreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.model.Review
import com.squareup.picasso.Picasso
import android.widget.ImageButton

class ReviewAdapter(
    private var reviews: List<Review>,
    private val onFavoriteClick: (Review) -> Unit,  // פונקציה ללחיצה על לב
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (Review) -> Unit,
    private val showEditOptions: Boolean = false
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = view.findViewById(R.id.bookTitleTextView)
        val bookDescription: TextView = view.findViewById(R.id.bookDescriptionTextView)
        val reviewText: TextView = view.findViewById(R.id.reviewTextView)
        val bookImage: ImageView = view.findViewById(R.id.bookImageView)
        val favoriteButton: ImageView = view.findViewById(R.id.favoriteButton)  // כפתור לב
        val editButton: ImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.bookTitle.text = review.bookTitle
        holder.bookDescription.text = review.bookDescription
        holder.reviewText.text = review.reviewText

        if (!review.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(review.imageUrl).into(holder.bookImage)
        } else {
            holder.bookImage.setImageResource(R.drawable.ic_placeholder)
        }

        // עדכון מצב הלב
        holder.favoriteButton.setImageResource(
            if (review.isFavorite) R.drawable.ic_check else R.drawable.ic_heart_outline
        )

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(review)
        }

        holder.editButton.setOnClickListener {
            onEditClick(review)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(review)
        }

        //  שליטה על נראות כפתורי עריכה/מחיקה
        if (showEditOptions) {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
        } else {
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}